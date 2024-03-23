/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.compilation.mapping;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.l3g7.griefer_utils.post_processor.processors.compilation.mapping.MappingEntries.MappedClass;
import dev.l3g7.griefer_utils.post_processor.processors.compilation.mapping.MappingEntries.MappedClassList;
import dev.l3g7.griefer_utils.post_processor.processors.compilation.mapping.MappingEntries.MappedField;
import dev.l3g7.griefer_utils.post_processor.processors.compilation.mapping.MappingEntries.MappedMethod;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

public class Mapper {

	private static final MappedClassList classes = new MappedClassList();
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static void loadMappings() throws IOException {
		File mappings = new File("build/1.8.9_stable_22.json");
		mappings.getParentFile().mkdirs();
		Collection<MappedClass> mappedClasses;

		if (mappings.exists()) {
			// Load mappings from file
			mappedClasses = gson.fromJson(new FileReader(mappings), new TypeToken<Collection<MappedClass>>() {}.getType());
			if (mappedClasses.isEmpty()) {
				// Probably invalid download, overwrite
				mappedClasses = new MappingCreator().createMappings();
				Files.writeString(mappings.toPath(), new Gson().toJson(mappedClasses));
			}
		} else {
			// Create and store mappings
			mappedClasses = new MappingCreator().createMappings();
			Files.writeString(mappings.toPath(), new Gson().toJson(mappedClasses));
		}

		classes.addAll(mappedClasses);
		classes.create();
	}

	/**
	 * Maps the name of a class from the source mapping to the target mapping.
	 */
	public static String mapClass(String name) {
		MappedClass mappedClass = classes.obfMap.get(name);
		if (mappedClass == null)
			// Assume class does not need mapping
			return name;

		return mappedClass.srgName();
	}

	/**
	 * Maps the name of a method from the source mapping to the target mapping.
	 */
	public static String mapMethodName(String owner, String name, String desc, boolean allowUnknown) {
		MappedClass mappedOwner = classes.obfMap.get(owner);
		if (mappedOwner == null)
			// Assume method does not need mapping as owner is not mapped
			return name;

		// Map name and descriptor
		MappedMethod method = mappedOwner.methodObfMap().get(name + desc);
		if (method == null)
			// Assume method does not need mapping
			return name;

		return method.srgName;
	}

	/**
	 * Maps the name of a field from the source mapping to the target mapping.
	 */
	public static String mapField(String owner, String name) {
		MappedClass mappedOwner = classes.obfMap.get(owner);
		if (mappedOwner == null)
			// Assume field does not need mapping as owner is not mapped
			return name;

		MappedField field = mappedOwner.fieldObfMap().get(name);
		if (field == null)
			// Assume field does not need mapping
			return name;

		return field.srgName();
	}

	/**
	 * Maps a type from the source mapping to the target mapping.
	 */
	public static Type mapType(Type type) {
		// Only map OBJECT types
		if (type.getSort() != Type.OBJECT)
			return type;

		MappedClass mappedType = classes.obfMap.get(type.getInternalName());
		if (mappedType == null)
			// Assume type does not need mapping
			return type;

		return Type.getObjectType(mappedType.srgName());
	}

	/**
	 * Maps all types from the source mapping to the target mapping.
	 */
	public static Type[] mapTypes(Type[] types) {
		Type[] mappedTypes = new Type[types.length];
		for (int i = 0; i < types.length; i++)
			mappedTypes[i] = mapType(types[i]);

		return mappedTypes;
	}

}