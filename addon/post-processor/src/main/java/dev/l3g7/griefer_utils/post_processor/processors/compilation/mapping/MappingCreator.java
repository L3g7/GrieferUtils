/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.compilation.mapping;

import dev.l3g7.griefer_utils.post_processor.processors.compilation.mapping.MappingEntries.MappedClass;
import dev.l3g7.griefer_utils.post_processor.processors.compilation.mapping.MappingEntries.MappedField;
import dev.l3g7.griefer_utils.post_processor.processors.compilation.mapping.MappingEntries.MappedMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MappingCreator {

	private final Map<String, MappedClass> classes = new HashMap<>();
	private final Map<String, List<MappedField>> srgFields = new HashMap<>();
	private final Map<String, List<MappedMethod>> srgMethods = new HashMap<>();

	public Collection<MappedClass> createMappings() throws IOException {
		ZipEntry entry;

		// Load searge mappings
		String url = "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/1.8.9/mcp-1.8.9-srg.zip";
		try (ZipInputStream in = new ZipInputStream(new URL(url).openConnection().getInputStream())) {
			while ((entry = in.getNextEntry()) != null) {
				if (entry.getName().equals("joined.srg")) {
					for (String line : new String(toByteArray(in), StandardCharsets.UTF_8).split("\r\n")) {
						if (line.startsWith("CL: ")) {
							// Load class obf -> srg mappings
							String[] parts = line.substring(4).split(" ");
							classes.put(parts[0], new MappedClass(parts[0], parts[1]));
						}
						else if (line.startsWith("FD: "))
							// Load field obf -> srg mappings using format
							// FD: obfClass/obfField unobfClass/srgField
							loadSrgMapping(line, 1, MappedField::new, MappedClass::fields, srgFields);
						else if (line.startsWith("MD: ")) {
							// Load method obf -> srg mappings using format
							// MD: obfClass/obfMethod obfDescriptor unobfClass/srgMethod unobfDescriptor
							MappedMethod method = loadSrgMapping(line, 2, MappedMethod::new, MappedClass::methods, srgMethods);
							String[] parts = line.substring(4).split(" ");
							method.obfDesc = parts[1];
						}
					}
				}
			}
		}

		return classes.values();
	}

	private <M> M loadSrgMapping(String line, int srgPosition, BiFunction<String, String, M> constructor, Function<MappedClass, ArrayList<M>> mappingStore, Map<String, List<M>> srgCache) {
		String[] parts = line.substring(4).split(" ");

		Member obf = new Member(parts[0]);
		Member srg = new Member(parts[srgPosition]);

		MappedClass owner = classes.get(obf.owner);
		M member = constructor.apply(obf.name, srg.name);

		mappingStore.apply(owner).add(member);
		srgCache.computeIfAbsent(srg.name, n -> new ArrayList<>()).add(member);

		return member;
	}

	private static class Member {

		public final String owner, name;

		public Member(String joined) {
			String[] parts = joined.split("/");
			this.name = parts[parts.length - 1];
			if (this.name == null)
				throw new IllegalArgumentException(joined + " has an invalid format!");

			this.owner = joined.substring(0, joined.length() - name.length() - 1);
		}

	}


	private static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		byte[] buffer = new byte[4096];
		int read;
		while ((read = input.read(buffer, 0, 4096)) >= 0)
			output.write(buffer, 0, read);

		return output.toByteArray();
	}

}