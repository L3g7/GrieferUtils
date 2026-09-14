/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.build;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.pymdk.mapper.Mapping;
import dev.pymdk.mapper.impl.LowLevelMapper;
import dev.pymdk.mapper.impl.MappingEntries.MappedClass;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Creates PyMDK mappings for all GrieferUtils classes with a Minecraft class as base class.
 */
public class MappingGenerator implements Opcodes {

	private static final Map<String, MappedClass> mappedClasses = new HashMap<>();

	public static void generateMappings(FileSystem fs) throws IOException {
		mappedClasses.clear();
		try (Stream<Path> paths = Files.walk(fs.getPath("/dev/l3g7/griefer_utils/"))) {
			Iterator<Path> iterator = paths.filter(path -> path.getFileName().toString().endsWith(".class")).iterator();
			while (iterator.hasNext()) {
				Path clazz = iterator.next();
				Class<?> cls = loadClass(clazz.toString());
				createMapping(cls);
			}
		}

		// Serialize
		JsonElement json = IO.GSON.toJsonTree(mappedClasses.values());
		for (JsonElement entry : json.getAsJsonArray())
			entry.getAsJsonObject().remove("imdName");

		IO.write(fs.getPath("/assets/griefer_utils/mappings-1.8.9-mcp.json")).value(IO.GSON.toJson(json));
	}

	private static @Nullable MappedClass createMapping(Class<?> cls) {
		String internalName = Type.getInternalName(cls);
		if (mappedClasses.get(internalName) instanceof MappedClass cached)
			return cached;

		if (isMinecraftClass(cls))
			return LowLevelMapper.classes.get(internalName, Mapping.UNOBFUSCATED);

		// Check if class hierarchy contains Minecraft class
		if (!hasMinecraftClassInHierarchy(cls))
			return null;

		// Build mapping entry
		List<String> baseClasses = new ArrayList<>();
		if (createMapping(cls.getSuperclass()) instanceof MappedClass mc)
			baseClasses.add(mc.getName(Mapping.UNOBFUSCATED));

		for (Class<?> itf : cls.getInterfaces())
			if (createMapping(itf) instanceof MappedClass mc)
				baseClasses.add(mc.getName(Mapping.UNOBFUSCATED));

		MappedClass mappedClass = new MappedClass(internalName, internalName);
		mappedClass.setBaseClasses(baseClasses.toArray(String[]::new));

		LowLevelMapper.classes.unobfMap.put(internalName, mappedClass);
		mappedClasses.put(internalName, mappedClass);
		return mappedClass;

	}

	private static boolean isMinecraftClass(Class<?> cls) {
		return cls.getName().startsWith("net.minecraft.") && !cls.getName().startsWith("net.minecraft.launchwrapper.");
	}

	private static Class<?> loadClass(String name) {
		try {
			name = name.replace('/', '.');
			if (name.endsWith(".class"))
				name = name.substring(0, name.length() - ".class".length());
			if (name.startsWith("."))
				name = name.substring(1);

			return Class.forName(name, false, MappingGenerator.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			throw Util.elevate(e);
		}
	}

	private static boolean hasMinecraftClassInHierarchy(Class<?> in) {
		if (isMinecraftClass(in))
			return true;

		if (in.getSuperclass() != null && hasMinecraftClassInHierarchy(in.getSuperclass()))
			return true;

		for (Class<?> itf : in.getInterfaces())
			if (hasMinecraftClassInHierarchy(itf))
				return true;

		return false;
	}

}
