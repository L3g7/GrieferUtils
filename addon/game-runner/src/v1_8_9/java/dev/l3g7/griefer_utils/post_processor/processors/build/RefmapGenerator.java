/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.build;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.AnnotationMeta;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.FieldMeta;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.MethodMeta;
import dev.pymdk.mapper.Mapping;
import dev.pymdk.mapper.impl.MappingEntries.MappedClass;
import dev.pymdk.mapper.impl.MappingEntries.MappedField;
import dev.pymdk.mapper.impl.MappingEntries.MappedMember;
import dev.pymdk.mapper.impl.MappingEntries.MappedMethod;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.pymdk.mapper.Mapping.*;
import static dev.pymdk.mapper.impl.LowLevelMapper.classes;
import static org.objectweb.asm.ClassReader.SKIP_CODE;

/**
 * Generates the refmap for LabyMod 3.
 */
public class RefmapGenerator {

	public static void generateRefmap(FileSystem fs) throws IOException {
		Map<String, Map<String, MemberWithOwner>> mappings = new HashMap<>();

		try (Stream<Path> paths = Files.walk(fs.getPath("/dev/l3g7/griefer_utils/"))) {
			Iterator<Path> iterator = paths.filter(path -> path.getFileName().toString().endsWith(".class")).iterator();
			while (iterator.hasNext()) {
				Path clazz = iterator.next();

				ClassNode node = new ClassNode();
				byte[] bytes = Files.readAllBytes(clazz);
				bytes[7 /* major_version */] = (byte) Math.min(bytes[7], 52 /* Java 1.8 */);
				new ClassReader(bytes).accept(node, SKIP_CODE);

				ClassMeta meta = new ClassMeta(node);
				if (!meta.hasAnnotation(Mixin.class))
					continue;

				if (meta.hasAnnotation(Bridge.ExclusiveTo.class)) {
					String[] version = meta.getAnnotation(Bridge.ExclusiveTo.class).getRawValue("value");
					if (version.length != 2 || version[1].equals("LABY_4"))
						continue;
				}

				mappings.put(meta.name, getMappingsFor(meta));
			}
		}

		JsonObject notch = new JsonObject();
		JsonObject searge = new JsonObject();
		for (Map.Entry<String, Map<String, MemberWithOwner>> clazz : mappings.entrySet()) {
			JsonObject notchClass = new JsonObject();
			JsonObject seargeClass = new JsonObject();

			for (Map.Entry<String, MemberWithOwner> entry : clazz.getValue().entrySet()) {
				notchClass.addProperty(entry.getKey(), entry.getValue().toMapped(OBFUSCATED));
				seargeClass.addProperty(entry.getKey(), entry.getValue().toMapped(INTERMEDIARY));
			}

			if (notchClass.isEmpty() && seargeClass.isEmpty())
				continue;

			notch.add(clazz.getKey(), notchClass);
			searge.add(clazz.getKey(), seargeClass);
		}

		JsonObject data = new JsonObject();
		data.add("notch", notch);
		data.add("searge", searge);

		JsonObject refMap = new JsonObject();
		refMap.add("mappings", searge);
		refMap.add("data", data);

		Files.writeString(fs.getPath("/assets/griefer_utils/refmap-labymod-3.json"), refMap.toString());
	}

	private static final Set<String> REMAPPED_ANNOTATIONS = ImmutableSet.of(
		"Lorg/spongepowered/asm/mixin/injection/Inject;",
		"Lorg/spongepowered/asm/mixin/injection/Redirect;",
		"Lorg/spongepowered/asm/mixin/injection/ModifyArg;",
		"Lorg/spongepowered/asm/mixin/injection/ModifyVariable;",
		"Lorg/spongepowered/asm/mixin/Shadow;"
	);

	private static Map<String, MemberWithOwner> getMappingsFor(ClassMeta classMeta) {
		Map<String, MemberWithOwner> mappings = new HashMap<>();

		String target = getMixinTarget(classMeta.getAnnotation(Mixin.class));

		for (MethodMeta method : classMeta.methods) {
			for (AnnotationMeta annotation : method.annotations()) {
				if (annotation.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;")) {
					if (shouldRemap(annotation))
						addRefMap(mappings, target, method.name() + method.desc(), true);
				} else if (REMAPPED_ANNOTATIONS.contains(annotation.desc)) {
					processAnnotation(mappings, target, annotation);
				}
			}
		}

		for (FieldMeta field : classMeta.fields) {
			AnnotationMeta shadow = field.getAnnotation(Shadow.class);
			if (shadow != null && shouldRemap(shadow))
				addRefMapField(mappings, "L" + target.replace('.', '/') + ";" + field.name() + ":" + field.desc(), true);
		}

		return mappings;
	}

	private static void processAnnotation(Map<String, MemberWithOwner> mappings, String owner, AnnotationMeta annotation) {
		if (shouldRemap(annotation))
			addRefMap(mappings, owner, annotation.getRawValue("method"), false);

		processAt(mappings, owner, annotation.getRawValue("at"));

		AnnotationNode sliceNode = annotation.getRawValue("slice");
		if (sliceNode == null)
			return;

		AnnotationMeta slice = new AnnotationMeta(sliceNode);
		processAt(mappings, owner, slice.getRawValue("from"));
		processAt(mappings, owner, slice.getRawValue("to"));
	}

	private static void processAt(Map<String, MemberWithOwner> mappings, String owner, AnnotationNode atNode) {
		if (atNode == null)
			return;

		AnnotationMeta at = new AnnotationMeta(atNode);
		String target = at.getRawValue("target");
		if (target != null) {
			if (at.getRawValue("value").equals("FIELD"))
				addRefMapField(mappings, target, false);
			else
				addRefMap(mappings, owner, target, false);
		}
	}

	private static String getMixinTarget(AnnotationMeta mixin) {
		Type value = mixin.getRawValue("value");
		if (value != null)
			return value.getClassName();

		return mixin.getRawValue("targets");
	}

	private static boolean shouldRemap(AnnotationMeta annotation) {
		return !Boolean.FALSE.equals(annotation.getRawValue("remap"));
	}

	private static void addRefMapField(Map<String, MemberWithOwner> mappings, String field, boolean isShadow) {
		int ownerEnd = field.indexOf(';');
		String owner = field.substring(1, ownerEnd);
		String name = field.substring(ownerEnd + 1, field.indexOf(':'));

		MemberWithOwner member = findField(owner, name);
		if (member != null)
			mappings.put(isShadow ? "<GU>" + field : field, member);
//		else
//			System.err.println("Field not found: " + name);
	}

	private static void addRefMap(Map<String, MemberWithOwner> mappings, String owner, String method, boolean isShadow) {
		int descStart = method.indexOf("(");
		int ownerStart = method.indexOf(';');

		String methodWithoutOwner = method;
		if (ownerStart != -1 && (descStart == -1 || ownerStart < descStart)) {
			// Extract owner from method string
			owner = method.substring(1, ownerStart);
			methodWithoutOwner = method.substring(ownerStart + 1);
		}

		MemberWithOwner member = findMethod(owner, methodWithoutOwner);
		if (member != null)
			mappings.put(isShadow ? "<GU>" + method : method, member);
//		else
//			System.err.println("Method not found: " + owner + " : " + method);
	}


	private static MemberWithOwner findMethod(String unnormalizedOwner, String method) {
		String owner = unnormalizedOwner.replace('.', '/');

		boolean hasDesc = method.indexOf('(') >= 0;
		return findMember(owner, mappedClass -> {
			MappedMethod found;
			if (hasDesc) {
				found = mappedClass.methods.unobfMap.get(method);
			} else {
				found = mappedClass.methods.unobfMap.entrySet().stream()
					.filter(e -> e.getKey().substring(0, e.getKey().indexOf('(')).equals(method))
					.map(Map.Entry::getValue)
					.findFirst()
					.orElse(null);
			}
			MappedClass mappedOwner = classes.unobfMap.get(owner);
			return found == null ? null : new MemberWithOwner(mappedOwner == null ? new MappedClass(owner, owner) : mappedOwner, found);
		});
	}

	private static MemberWithOwner findField(String unnormalizedOwner, String fieldName) {
		String owner = unnormalizedOwner.replace('.', '/');
		return findMember(owner, mappedClass -> {
			MappedField found = mappedClass.fields.unobfMap.get(fieldName);
			MappedClass mappedOwner = classes.unobfMap.get(owner);
			return found == null ? null : new MemberWithOwner(mappedOwner == null ? new MappedClass(owner, owner) : mappedOwner, found);
		});
	}

	private static MemberWithOwner findMember(String owner, Function<MappedClass, MemberWithOwner> lookup) {
		MappedClass mappedClass = classes.unobfMap.get(owner);
		if (mappedClass != null) {
			MemberWithOwner member = lookup.apply(mappedClass);
			if (member != null)
				return member;
		}

		Class<?> clazz;
		try {
			clazz = Class.forName(owner.replace('/', '.'), false, RefmapGenerator.class.getClassLoader());
		} catch (ClassNotFoundException e) {
//			System.err.println("Can't load class " + e.getMessage());
			return null;
		}

		if (clazz == Object.class)
			return null;

		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null) {
			MemberWithOwner member = findMember(superclass.getName().replace('.', '/'), lookup);
			if (member != null)
				return member;
		}

		for (Class<?> itf : clazz.getInterfaces()) {
			MemberWithOwner member = findMember(itf.getName().replace('.', '/'), lookup);
			if (member != null)
				return member;
		}

		return null;
	}

	private record MemberWithOwner(MappedClass owner, MappedMember member) {

		public String toMapped(Mapping mapping) {
			String format = member instanceof MappedField ? "L%s;%s:%s" : "L%s;%s%s";
			return format.formatted(owner.getName(mapping == INTERMEDIARY ? UNOBFUSCATED : mapping), member.getName(mapping), member.getDesc(mapping));
		}

	}

}
