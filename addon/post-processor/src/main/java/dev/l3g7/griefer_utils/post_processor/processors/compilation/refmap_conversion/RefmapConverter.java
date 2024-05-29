/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.compilation.refmap_conversion;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import dev.l3g7.griefer_utils.api.mapping.Mapper;
import dev.l3g7.griefer_utils.post_processor.processors.CompilationPostProcessor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.objectweb.asm.ClassReader.*;

/**
 * Moves all refmaps into a separate folder and generates one for LabyMod 3.
 */
public class RefmapConverter extends CompilationPostProcessor {

	public static final RefmapConverter INSTANCE = new RefmapConverter();

	private RefmapConverter() {
		System.setProperty("griefer_utils.custom_ssl", "false");
		Mapper.loadMappings("1.8.9", "22", new File("build/1.8.9_stable_22.json"));
	}

	@Override
	public void apply(FileSystem fs) throws IOException {
		// Move refmaps
		Files.createDirectory(fs.getPath("/refmaps/"));
		try (Stream<Path> stream = Files.list(fs.getPath("/"))) {
			stream
				.filter(p -> p.toString().endsWith("-GrieferUtils.refmap.json"))
				.forEach(path -> {
					String name = path.getFileName().toString();
					try {
						Files.move(path, fs.getPath("/refmaps/" + name.substring(0, name.length() - 25) + ".json"));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		}

		// Generate LabyMod 3 refmap
		Path refmapIn = fs.getPath("/refmaps/1.8.9.json");
		Path refmapOut = fs.getPath("/refmaps/LabyMod-3.json");

		JsonObject refmap;
		try (Reader in = new InputStreamReader(Files.newInputStream(refmapIn), UTF_8)) {
			refmap = Streams.parse(new JsonReader(in)).getAsJsonObject();
		}

		JsonObject seargeMappings = refmap.get("data").getAsJsonObject().get("searge").getAsJsonObject();
		for (String key : seargeMappings.keySet()) {
			JsonObject mappings = seargeMappings.get(key).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : mappings.entrySet()) {
				String mapping = entry.getValue().getAsString();

				// Parse mapping
				int ownerEndIdx = 0;
				int nameEndIdx = 0;
				boolean method = true;
				for (int i = 0; i < mapping.length(); i++) {
					char c = mapping.charAt(i);
					if (c == ';')
						ownerEndIdx = i + 1;
					else if (c == '(') {
						nameEndIdx = i;
						break;
					} else if (c == ':') {
						nameEndIdx = i;
						method = false;
						break;
					}
				}

				// Convert mapping
				String owner = Type.getType(mapping.substring(0, ownerEndIdx)).getInternalName();
				String name = mapping.substring(ownerEndIdx, nameEndIdx);
				String desc = mapping.substring(nameEndIdx);
				String mappedOwner = ReferenceMapper.mapClass(owner);
				String mappedName;

				if (method) {
					mappedName = ReferenceMapper.mapMethodName(owner, name, desc, true);

					if (mappedName.equals(name) && !name.equals("<init>")) {
						// Resolve @InheritedInvoke
						owner = getInheritedInvokeOwner(entry.getKey(), Files.readAllBytes(fs.getPath(key + ".class")));
						mappedName = ReferenceMapper.mapMethodName(owner, name, desc, false);
					}

					Type[] argumentTypes = ReferenceMapper.mapTypes(Type.getArgumentTypes(desc));
					Type returnType = ReferenceMapper.mapType(Type.getReturnType(desc));
					desc = Type.getMethodDescriptor(returnType, argumentTypes);
				} else
					mappedName = ReferenceMapper.mapField(owner, name);

				String newMapping = "L" + mappedOwner + ";" + mappedName + desc;
				entry.setValue(new JsonPrimitive(newMapping));
			}
		}

		refmap.add("mappings", seargeMappings);

		// Write new refmap
		try (OutputStream out = Files.newOutputStream(refmapOut)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			out.write(gson.toJson(refmap).getBytes(StandardCharsets.ISO_8859_1));
		}
	}

	private String getInheritedInvokeOwner(String target, byte[] classBytes) {
		ClassReader r = new ClassReader(classBytes);
		ClassNode node = new ClassNode();
		r.accept(node, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);

		for (MethodNode methodNode : node.methods) {
			if (methodNode.invisibleAnnotations == null)
				continue;

			// Find right method
			for (AnnotationNode inject : methodNode.visibleAnnotations) {
				if (inject.desc.startsWith("Lorg/spongepowered/asm/mixin/injection/")) {
					AnnotationNode at;
					if (getAnnotationValue(inject, "at") instanceof AnnotationNode v)
						at = v;
					else
						at = (AnnotationNode) ((List<?>) getAnnotationValue(inject, "at")).get(0);

					if (!target.equals(getAnnotationValue(at, "target")))
						continue;

					// Find @InheritedInvoke
					for (AnnotationNode annotation : methodNode.invisibleAnnotations)
						if (annotation.desc.equals("Ldev/l3g7/griefer_utils/injection/InheritedInvoke;"))
							return ((Type) getAnnotationValue(annotation, "value")).getInternalName();

				}
			}
		}
		return null;
	}

	private Object getAnnotationValue(AnnotationNode annotation, String key) {
		for (int i = 0; i < annotation.values.size(); i += 2)
			if (annotation.values.get(i).equals(key))
				return annotation.values.get(i + 1);

		throw new NoSuchElementException(key);
	}

}
