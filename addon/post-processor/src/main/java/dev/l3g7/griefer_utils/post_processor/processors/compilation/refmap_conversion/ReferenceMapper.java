/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.compilation.refmap_conversion;

import dev.l3g7.griefer_utils.api.mapping.MappingEntries;
import org.objectweb.asm.Type;

import java.util.NoSuchElementException;

import static dev.l3g7.griefer_utils.api.mapping.Mapper.classes;
import static dev.l3g7.griefer_utils.api.mapping.Mapping.OBFUSCATED;
import static dev.l3g7.griefer_utils.api.mapping.Mapping.SEARGE;

public class ReferenceMapper {

	public static String mapClass(String name) {
		MappingEntries.MappedClass mappedClass = classes.obfMap.get(name);
		if (mappedClass == null) {
			if (name.startsWith("net/labymod/"))
				return name;

			throw new NoSuchElementException("Could not find mapping for class " + name);
		}

		return mappedClass.getName(SEARGE);
	}

	public static String mapMethodName(String owner, String name, String desc, boolean allowUnknown) {
		MappingEntries.MappedClass mappedOwner = classes.obfMap.get(owner);
		if (mappedOwner == null) {
			if (owner.startsWith("net/labymod/") && allowUnknown)
				return name;

			throw new NoSuchElementException("Could not find mapping for class " + owner);
		}

		// Map name and descriptor
		MappingEntries.MappedMethod method = mappedOwner.methods.get(name + desc, OBFUSCATED);
		if (method == null) {
			if (allowUnknown)
				return name;

			throw new NoSuchElementException("Could not find mapping for method " + name + desc + " in class " + mappedOwner.getName(SEARGE) + " (" + owner + ")");
		}

		return method.getName(SEARGE);
	}

	public static String mapField(String owner, String name) {
		MappingEntries.MappedClass mappedOwner = classes.obfMap.get(owner);
		if (mappedOwner == null)
			throw new NoSuchElementException("Could not find mapping for class " + owner);

		MappingEntries.MappedField field = mappedOwner.fields.get(name, OBFUSCATED);
		if (field == null)
			throw new NoSuchElementException("Could not find mapping for field " + name + " in class " + mappedOwner.getName(SEARGE) + " (" + owner + ")");

		return field.getName(SEARGE);
	}

	public static Type mapType(Type type) {
		// Only map OBJECT types
		if (type.getSort() != Type.OBJECT)
			return type;

		MappingEntries.MappedClass mappedType = classes.obfMap.get(type.getInternalName());
		if (mappedType == null) {
			if (type.getInternalName().contains("/"))
				// Not a minecraft class
				return type;

			throw new NoSuchElementException("Could not find mapping for class " + type.getInternalName());
		}

		return Type.getObjectType(mappedType.getName(SEARGE));
	}

	public static Type[] mapTypes(Type[] types) {
		Type[] mappedTypes = new Type[types.length];
		for (int i = 0; i < types.length; i++)
			mappedTypes[i] = mapType(types[i]);

		return mappedTypes;
	}

}
