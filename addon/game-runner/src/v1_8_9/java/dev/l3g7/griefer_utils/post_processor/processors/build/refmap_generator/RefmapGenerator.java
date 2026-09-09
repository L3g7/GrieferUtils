/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.build.refmap_generator;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.AnnotationMeta;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.FieldMeta;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.MethodMeta;
import dev.l3g7.griefer_utils.post_processor.processors.build.refmap_generator.MappingHelper.NonRemappedEntry;
import dev.l3g7.griefer_utils.post_processor.processors.build.refmap_generator.MappingHelper.RefmapEntry;
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
import java.util.stream.Stream;

import static dev.l3g7.griefer_utils.post_processor.processors.build.refmap_generator.MappingHelper.findField;
import static dev.l3g7.griefer_utils.post_processor.processors.build.refmap_generator.MappingHelper.findMethod;
import static dev.pymdk.mapper.Mapping.INTERMEDIARY;
import static dev.pymdk.mapper.Mapping.OBFUSCATED;
import static org.objectweb.asm.ClassReader.SKIP_CODE;

/**
 * Generates the refmap for LabyMod 3.
 */
public class RefmapGenerator {

	public static void generateRefmap(FileSystem fs) throws IOException {
		Map<String, Map<String, RefmapEntry>> mappings = new HashMap<>();

		// Collect
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

		// Write as refmap
		JsonObject notch = new JsonObject();
		JsonObject searge = new JsonObject();
		for (Map.Entry<String, Map<String, RefmapEntry>> clazz : mappings.entrySet()) {
			JsonObject notchClass = new JsonObject();
			JsonObject seargeClass = new JsonObject();

			for (Map.Entry<String, RefmapEntry> entry : clazz.getValue().entrySet()) {
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

	private static Map<String, RefmapEntry> getMappingsFor(ClassMeta classMeta) {
		Map<String, RefmapEntry> mappings = new HashMap<>();


		AnnotationMeta mixin = classMeta.getAnnotation(Mixin.class);
		Type value = mixin.getRawValue("value");
		String target = value != null ? value.getClassName() : mixin.getRawValue("targets");

		for (MethodMeta method : classMeta.methods) {
			for (AnnotationMeta annotation : method.annotations()) {
				if (annotation.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;")) {
					addRefMapMethod(mappings, target, method.name() + method.desc(), annotation);
				} else if (REMAPPED_ANNOTATIONS.contains(annotation.desc)) {
					processAnnotation(mappings, target, annotation);
				}
			}
		}

		for (FieldMeta field : classMeta.fields) {
			AnnotationMeta shadow = field.getAnnotation(Shadow.class);
			if (shadow != null)
				addRefMapField(mappings, "L" + target.replace('.', '/') + ";" + field.name() + ":" + field.desc(), shadow);
		}

		return mappings;
	}

	private static void processAnnotation(Map<String, RefmapEntry> mappings, String owner, AnnotationMeta annotation) {
		addRefMapMethod(mappings, owner, annotation.getRawValue("method"), annotation);

		processAt(mappings, owner, annotation.getRawValue("at"));

		AnnotationNode sliceNode = annotation.getRawValue("slice");
		if (sliceNode == null)
			return;

		AnnotationMeta slice = new AnnotationMeta(sliceNode);
		processAt(mappings, owner, slice.getRawValue("from"));
		processAt(mappings, owner, slice.getRawValue("to"));
	}

	private static void processAt(Map<String, RefmapEntry> mappings, String owner, AnnotationNode atNode) {
		if (atNode == null)
			return;

		AnnotationMeta at = new AnnotationMeta(atNode);
		String target = at.getRawValue("target");
		if (target != null) {
			if (at.getRawValue("value").equals("FIELD"))
				addRefMapField(mappings, target, at);
			else
				addRefMapMethod(mappings, owner, target, at);
		}
	}

	private static boolean skipRemap(AnnotationMeta annotation) {
		return Boolean.FALSE.equals(annotation.getRawValue("remap"));
	}

	private static void addRefMapField(Map<String, RefmapEntry> mappings, String field, AnnotationMeta annotation) {
		int descStart = field.indexOf(':');
		int ownerEnd = field.indexOf(';');
		String owner = field.substring(1, ownerEnd);
		String name = field.substring(ownerEnd + 1, descStart);

		String key = annotation.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;") ? "<GU>" + field : field;

		RefmapEntry entry = skipRemap(annotation) ? null : findField(owner, name);
		if (entry != null) {
			mappings.put(key, entry);
			return;
		}

		mappings.put(key, new NonRemappedEntry(owner, name, field.substring(descStart + 1)));
	}

	private static void addRefMapMethod(Map<String, RefmapEntry> mappings, String owner, String method, AnnotationMeta annotation) {
		int descStart = method.indexOf("(");
		int ownerStart = method.indexOf(';');

		String methodWithoutOwner = method;
		if (ownerStart != -1 && (descStart == -1 || ownerStart < descStart)) {
			// Extract owner from method string
			owner = method.substring(1, ownerStart);
			methodWithoutOwner = method.substring(ownerStart + 1);
		}

		String key = annotation.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;") ? "<GU>" + method : method;

		RefmapEntry entry = skipRemap(annotation) ? null : findMethod(owner, methodWithoutOwner);
		if (entry != null) {
			mappings.put(key, entry);
			return;
		}

		if (descStart != -1) {
			descStart = methodWithoutOwner.indexOf('(');
			mappings.put(key, new NonRemappedEntry(owner, methodWithoutOwner.substring(0, descStart), methodWithoutOwner.substring(descStart)));
		}
	}

}
