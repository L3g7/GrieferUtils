/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.core.mapping;

import dev.l3g7.griefer_utils.core.misc.CustomSSLSocketFactoryProvider;
import dev.l3g7.griefer_utils.core.util.ArrayUtil;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads and merges mappings.
 */
public class MappingCreator {

	/**
	 * A map storing all classes. The key is the obfuscated class name.
	 */
	private final Map<String, MappingEntries.MappedClass> classes = new HashMap<>();

	/**
	 * A cache storing all searge field names and their associated fields.
	 */
	private final Map<String, List<MappingEntries.MappedField>> srgFields = new HashMap<>();

	/**
	 * A cache storing all searge method names and their associated methods.
	 */
	private final Map<String, List<MappingEntries.MappedMethod>> srgMethods = new HashMap<>();

	/**
	 * Downloads and merges the specified mappings.
	 */
	public Collection<MappingEntries.MappedClass> createMappings(String minecraftVersion, String mappingVersion) throws IOException, GeneralSecurityException {
		ZipEntry entry;

		// Load searge mappings
		try (ZipInputStream in = getZipInputStream(String.format("https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/%s/mcp-%s-srg.zip", minecraftVersion, minecraftVersion))) {
			while ((entry = in.getNextEntry()) != null) {
				if (entry.getName().equals("joined.srg")) {
					for (String line : new String(IOUtils.toByteArray(in), StandardCharsets.UTF_8).split("\r\n")) {
						if (line.startsWith("CL: ")) {
							// Load class obf -> srg mappings
							String[] parts = line.substring(4).split(" ");
							classes.put(parts[0], new MappingEntries.MappedClass(parts[0], parts[1]));
						}
						else if (line.startsWith("FD: "))
							// Load field obf -> srg mappings using format
							// FD: obfClass/obfField unobfClass/srgField
							loadSrgMapping(line, 1, MappingEntries.MappedField::new, c -> c.fields, srgFields);
						else if (line.startsWith("MD: ")) {
							// Load method obf -> srg mappings using format
							// MD: obfClass/obfMethod obfDescriptor unobfClass/srgMethod unobfDescriptor
							MappingEntries.MappedMethod method = loadSrgMapping(line, 2, MappingEntries.MappedMethod::new, c -> c.methods, srgMethods);
							String[] parts = line.substring(4).split(" ");
							method.obfDesc = parts[1];
							method.unobfDesc = parts[3];
						}
					}
				}
			}
		}

		// Load unobfuscated mappings
		String unobfMappingURL = String.format("https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/%s-%s/mcp_stable-%s-%s.zip", mappingVersion, minecraftVersion, mappingVersion, minecraftVersion);
		try (ZipInputStream in = getZipInputStream(unobfMappingURL)) {
			while ((entry = in.getNextEntry()) != null) {
				if (entry.getName().equals("fields.csv"))
					loadUnobfMapping(in, "field", srgFields, (f, u) -> f.unobfName = u);
				else if (entry.getName().equals("methods.csv"))
					loadUnobfMapping(in, "func", srgMethods, (f, u) -> f.unobfName = u);
			}
		}

		return classes.values();
	}

	/**
	 * Reads a zip file from the given URL using the CustomSSLSocketFactory.<br>
	 * This is required because MinecraftForge uses LetsEncrypt, which is not supported in 8u51, the default java version in Minecraft.
	 * As a result, the SSL certificate cannot be validated using the native certificates.
	 * @see CustomSSLSocketFactoryProvider
	 */
	private ZipInputStream getZipInputStream(String url) throws IOException {
		HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
		conn.setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());

		return new ZipInputStream(conn.getInputStream());
	}

	/**
	 * Loads an obf -> srg mappings in a .srg file with one of the following formats:
	 * <p>
	 *     <code>
	 *     FD: obfClass/obfField unobfClass/srgField
	 *     </code> for fields
	 *     <br>
	 *     <code>
	 *       MD: obfClass/obfMethod obfDescriptor unobfClass/srgMethod unobfDescriptor
	 *     </code> for methods
	 * </p>
	 *
	 * @param line the line containing the mapping.
	 * @param srgPosition the position of the searge name.
	 * @param constructor the constructor for the class associated with the member type.
	 * @param mappingStore a function returning the list storing the members.
	 * @param srgCache the cache storing the searge member names and their associated members.
	 * @return the loaded member.
	 */
	private <M extends MappingEntries.MappedMember> M loadSrgMapping(String line, int srgPosition, BiFunction<String, String, M> constructor, Function<MappingEntries.MappedClass, MappingEntries.MappedList<M>> mappingStore, Map<String, List<M>> srgCache) {
		String[] parts = line.substring(4).split(" ");

		Member obf = new Member(parts[0]);
		Member srg = new Member(parts[srgPosition]);

		MappingEntries.MappedClass owner = classes.get(obf.owner);
		M member = constructor.apply(obf.name, srg.name);

		mappingStore.apply(owner).add(member);
		srgCache.computeIfAbsent(srg.name, n -> new ArrayList<>()).add(member);

		return member;
	}

	/**
	 * Loads all srg -> unobf mappings in a .csv file with the format <code>searge,name,side,desc</code>, seperated using <code>\r\n</code>.
	 *
	 * @param in an input stream containing the .csv file.
	 * @param prefix the prefix of the searge names.
	 * @param srgCache the cache storing the searge member names and their associated members.
	 * @param callback a callback storing the unobfuscated name in the member.
	 */
	private <M> void loadUnobfMapping(InputStream in, String prefix, Map<String, List<M>> srgCache, BiConsumer<M, String> callback) throws IOException {
		for (String line : new String(IOUtils.toByteArray(in), StandardCharsets.UTF_8).split("\r\n")) {
			if (!line.startsWith(prefix))
				continue;

			String[] parts = line.split(",");
			for (M member : srgCache.get(parts[0]))
				callback.accept(member, parts[1]);
		}
	}

	/**
	 * A string representation of a class member in the format <code>"class/member"</code>.
	 * The class may have additional slashes in it, so the string is split at the last slash.
	 */
	private static class Member {

		public final String owner, name;

		public Member(String joined) {
			this.name = ArrayUtil.last(joined.split("/"));
			if (this.name == null)
				throw new IllegalArgumentException(joined + " has an invalid format!");

			this.owner = joined.substring(0, joined.length() - name.length() - 1);
		}

	}

}