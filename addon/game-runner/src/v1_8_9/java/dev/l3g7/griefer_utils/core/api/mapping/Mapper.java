/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.mapping;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.api.bridges.MinecraftBridge;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.Collection;

public class Mapper {

	public static final MappingEntries.MappedList<MappingEntries.MappedClass> classes = new MappingEntries.MappedList<>();

	public static void loadMappings(String minecraftVersion, String mappingVersion) {
		loadMappings(minecraftVersion, mappingVersion, new File(MinecraftBridge.minecraftBridge.assetsDir(), String.format("griefer_utils/mappings/%s_stable_%s.json", minecraftVersion, mappingVersion)));
	}

	public static void loadMappings(String minecraftVersion, String mappingVersion, File mappings) {
		try {
			Collection<MappingEntries.MappedClass> mappedClasses;

			if (mappings.exists()) {
				// Load mappings from file
				mappedClasses = IOUtil.gson.fromJson(new FileReader(mappings), new TypeToken<Collection<MappingEntries.MappedClass>>() {}.getType());
				if (mappedClasses.isEmpty()) {
					// Probably invalid download, overwrite
					mappedClasses = new MappingCreator().createMappings(minecraftVersion, mappingVersion);
					IOUtil.write(mappings, new Gson().toJson(mappedClasses));
				}
			} else {
				// Create and store mappings
				mappedClasses = new MappingCreator().createMappings(minecraftVersion, mappingVersion);
				IOUtil.write(mappings, new Gson().toJson(mappedClasses));
			}

			classes.addAll(mappedClasses);
			classes.create();

		} catch (IOException | GeneralSecurityException e) {
			throw Util.elevate(e, "Could not load mappings!");
		}
	}

	public static boolean isObfuscated() {
		return LabyBridge.labyBridge.obfuscated();
	}

	/**
	 * Maps the name of a class from the source mapping to the target mapping.
	 */
	public static String mapClass(String name, Mapping sourceMapping, Mapping targetMapping) {
		MappingEntries.MappedClass mappedClass = classes.get(name, sourceMapping);
		if (mappedClass == null)
			// Assume class does not need mapping
			return name;

		return mappedClass.getName(targetMapping);
	}

	/**
	 * Maps the name of a method from the source mapping to the target mapping.
	 */
	public static String mapMethodName(String owner, String name, String desc, Mapping sourceMapping, Mapping targetMapping) {
		if (targetMapping == Mapping.OBFUSCATED)
			owner = mapClass(owner, Mapping.OBFUSCATED, Mapping.UNOBFUSCATED);

		MappingEntries.MappedClass mappedOwner = classes.get(owner, sourceMapping);
		if (mappedOwner == null)
			// Assume method does not need mapping as owner is not mapped
			return name;

		// Map name and descriptor
		MappingEntries.MappedMethod method = mappedOwner.methods.get(name + desc, sourceMapping);
		if (method == null)
			// Assume method does not need mapping
			return name;

		return method.getName(targetMapping);
	}

	/**
	 * Maps the name of a method from the source mapping to the target mapping.
	 */
	public static String mapMethodName(Method method, Mapping sourceMapping, Mapping targetMapping) {
		return mapMethodName(Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method), sourceMapping, targetMapping);
	}

	/**
	 * Maps the name of a field from the source mapping to the target mapping.
	 */
	public static String mapField(String owner, String name, Mapping sourceMapping, Mapping targetMapping) {
		if (targetMapping == Mapping.OBFUSCATED)
			owner = mapClass(owner, Mapping.OBFUSCATED, Mapping.UNOBFUSCATED);

		MappingEntries.MappedClass mappedOwner = classes.get(owner, sourceMapping);
		if (mappedOwner == null)
			// Assume field does not need mapping as owner is not mapped
			return name;

		MappingEntries.MappedField field = mappedOwner.fields.get(name, sourceMapping);
		if (field == null)
			// Assume field does not need mapping
			return name;

		return field.getName(targetMapping);
	}

	/**
	 * Maps the name of a field from the source mapping to the target mapping.
	 */
	public static String mapField(Class<?> owner, String name, Mapping sourceMapping, Mapping targetMapping) {
		return mapField(Type.getInternalName(owner), name, sourceMapping, targetMapping);
	}

	/**
	 * Maps a type from the source mapping to the target mapping.
	 */
	public static Type mapType(Type type, Mapping sourceMapping, Mapping targetMapping) {
		// Only map OBJECT types
		if (type.getSort() != Type.OBJECT)
			return type;

		MappingEntries.MappedClass mappedType = classes.get(type.getInternalName(), sourceMapping);
		if (mappedType == null)
			// Assume type does not need mapping
			return type;

		return Type.getObjectType(mappedType.getName(targetMapping));
	}

	/**
	 * Maps all types from the source mapping to the target mapping.
	 */
	public static Type[] mapTypes(Type[] types, Mapping sourceMapping, Mapping targetMapping) {
		Type[] mappedTypes = new Type[types.length];
		for (int i = 0; i < types.length; i++)
			mappedTypes[i] = mapType(types[i], sourceMapping, targetMapping);

		return mappedTypes;
	}

}