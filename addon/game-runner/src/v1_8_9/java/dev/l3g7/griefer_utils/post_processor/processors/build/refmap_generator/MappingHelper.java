/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.build.refmap_generator;

import dev.pymdk.mapper.Mapping;
import dev.pymdk.mapper.impl.MappingEntries;
import dev.pymdk.mapper.impl.MappingEntries.MappedClass;

import java.util.Map;
import java.util.function.Function;

import static dev.pymdk.mapper.Mapping.INTERMEDIARY;
import static dev.pymdk.mapper.Mapping.UNOBFUSCATED;
import static dev.pymdk.mapper.impl.LowLevelMapper.classes;

class MappingHelper {

	public static MemberWithOwner findMethod(String unnormalizedOwner, String method) {
		String owner = unnormalizedOwner.replace('.', '/');

		boolean hasDesc = method.indexOf('(') >= 0;
		return findMember(owner, mappedClass -> {
			MappingEntries.MappedMethod found;
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

	public static MemberWithOwner findField(String unnormalizedOwner, String fieldName) {
		String owner = unnormalizedOwner.replace('.', '/');
		return findMember(owner, mappedClass -> {
			MappingEntries.MappedField found = mappedClass.fields.unobfMap.get(fieldName);
			MappedClass mappedOwner = classes.unobfMap.get(owner);
			return found == null ? null : new MemberWithOwner(mappedOwner == null ? new MappedClass(owner, owner) : mappedOwner, found);
		});
	}

	public static MemberWithOwner findMember(String owner, Function<MappedClass, MemberWithOwner> lookup) {
		MappedClass mappedClass = classes.unobfMap.get(owner);
		if (mappedClass != null) {
			MemberWithOwner member = lookup.apply(mappedClass);
			if (member != null)
				return member;
		}

		Class<?> clazz;
		try {
			clazz = Class.forName(owner.replace('/', '.'), false, MappingHelper.class.getClassLoader());
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

	interface RefmapEntry {
		String toMapped(Mapping mapping);
	}

	record MemberWithOwner(MappedClass owner, MappingEntries.MappedMember member) implements RefmapEntry {

		public String toMapped(Mapping mapping) {
			String format = member instanceof MappingEntries.MappedField ? "L%s;%s:%s" : "L%s;%s%s";
			return format.formatted(owner.getName(mapping == INTERMEDIARY ? UNOBFUSCATED : mapping), member.getName(mapping), member.getDesc(mapping));
		}

	}

	record NonRemappedEntry(String owner, String name, String desc) implements RefmapEntry {

		@Override
		public String toMapped(Mapping mapping) {
			boolean isField = !desc.startsWith("(");

			String format = isField ? "L%s;%s:%s" : "L%s;%s%s";
			String mappedDesc = isField ? mapStandaloneFieldDesc(desc, mapping) : mapStandaloneMethodDesc(desc, mapping);
			return format.formatted(owner.replace('.', '/'), name, mappedDesc);
		}

	}

	// Copied from LowLevelMapper but with a custom target mapping

	/**
	 * Maps a field descriptor.
	 */
	private static String mapStandaloneFieldDesc(String desc, Mapping targetMapping) {
		if (desc.charAt(0) == '[')
			return "[" + mapStandaloneFieldDesc(desc.substring(1), targetMapping);
		if (desc.charAt(0) != 'L') {
			if (desc.length() != 1)
				throw new UnsupportedOperationException(desc);
			return desc;
		}

		String name = desc.substring(1, desc.length() - 1);
		MappedClass mappedClass = classes.get(name, UNOBFUSCATED);
		if (mappedClass == null)
			return desc;

		return "L" + mappedClass.getName(targetMapping) + ";";
	}

	/**
	 * Maps a method descriptor.
	 */
	private static String mapStandaloneMethodDesc(String desc, Mapping targetMapping) {
		StringBuilder builder = new StringBuilder(desc.length());
		builder.append('(');
		char[] chars = desc.toCharArray();
		for (int index = 1; index < desc.length(); index++) {
			if (chars[index] != 'L') {
				builder.append(chars[index]);
				continue;
			}

			int endIndex = index;
			while (chars[endIndex] != ';') endIndex++;

			String name = desc.substring(index, endIndex + 1);
			builder.append(mapStandaloneFieldDesc(name, targetMapping));
			index = endIndex;
		}

		return builder.toString();
	}

}
