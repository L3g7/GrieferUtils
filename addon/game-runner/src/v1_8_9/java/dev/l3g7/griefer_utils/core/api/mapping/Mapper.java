/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.mapping;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.pymdk.mapper.Mapping;
import dev.pymdk.mapper.impl.MappingEntries.MappedClass;
import dev.pymdk.mapper.impl.MappingEntries.MappedField;
import dev.pymdk.mapper.impl.MappingEntries.MappedMethod;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.nio.file.Path;

import static dev.pymdk.mapper.Mapping.OBFUSCATED;
import static dev.pymdk.mapper.Mapping.UNOBFUSCATED;
import static dev.pymdk.mapper.impl.LowLevelMapper.classes;

public class Mapper {

	public static void loadMappings(Path assetsDir, boolean registerPostProcessor) {
		MappingLoader.loadMappings(assetsDir, registerPostProcessor);
	}

	public static boolean isObfuscated() {
		return LabyBridge.labyBridge.obfuscated();
	}

	/**
	 * Maps the name of a class from the source mapping to the target mapping.
	 */
	public static String mapClass(String name, Mapping sourceMapping, Mapping targetMapping) {
		MappedClass mappedClass = classes.get(name, sourceMapping);
		if (mappedClass == null)
			// Assume class does not need mapping
			return name;

		return mappedClass.getName(targetMapping);
	}

	/**
	 * Maps the name of a method from the source mapping to the target mapping.
	 */
	public static String mapMethodName(String owner, String name, String desc, Mapping sourceMapping, Mapping targetMapping) {
		if (targetMapping == OBFUSCATED)
			owner = mapClass(owner, OBFUSCATED, UNOBFUSCATED);

		MappedClass mappedOwner = classes.get(owner, sourceMapping);
		if (mappedOwner == null)
			// Assume method does not need mapping as owner is not mapped
			return name;

		// Map name and descriptor
		MappedMethod method = mappedOwner.getMethodRecursive(name + desc, sourceMapping);
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
		if (targetMapping == OBFUSCATED)
			owner = mapClass(owner, OBFUSCATED, UNOBFUSCATED);

		MappedClass mappedOwner = classes.get(owner, sourceMapping);
		if (mappedOwner == null)
			// Assume field does not need mapping as owner is not mapped
			return name;

		MappedField field = mappedOwner.getFieldRecursive(name, sourceMapping);
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

		MappedClass mappedType = classes.get(type.getInternalName(), sourceMapping);
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