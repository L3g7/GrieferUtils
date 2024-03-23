/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.compilation.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MappingEntries {

	public record MappedClass(
		ArrayList<MappedField> fields, Map<String, MappedField> fieldObfMap,
		ArrayList<MappedMethod> methods, Map<String, MappedMethod> methodObfMap,
		String obfName, String srgName) {

		MappedClass(String obf, String srg) {
			this(new ArrayList<>(), new HashMap<>(), new ArrayList<>(), new HashMap<>(), obf, srg);
		}

		void create() {
			for (MappedField member : fields)
				fieldObfMap.put(member.obfName, member);

			for (MappedMethod method : methods)
				methodObfMap.put(method.obfName + method.obfDesc, method);
		}

	}

	public static class MappedClassList extends ArrayList<MappedClass> {

		final Map<String, MappedClass> obfMap = new HashMap<>();

		public void create() {
			for (MappedClass member : this) {
				obfMap.put(member.obfName, member);
				member.create();
			}
		}

	}

	public record MappedField(String obfName, String srgName) {}

	public static class MappedMethod {

		final String obfName;
		final String srgName;

		String obfDesc;

		MappedMethod(String obf, String srg) {
			this.obfName = obf;
			this.srgName = srg;
		}

	}

}
