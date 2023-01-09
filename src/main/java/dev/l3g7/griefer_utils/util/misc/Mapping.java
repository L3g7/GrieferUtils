/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.util.misc;

import com.google.gson.reflect.TypeToken;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.labymod.core.asm.LabyModCoreMod;
import org.objectweb.asm.Type;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.util.Util.elevate;

/**
 * A provider for MCP -> Notch and MCP -> SRG mappings.
 */
public class Mapping {

	private static final boolean obfuscated = LabyModCoreMod.isObfuscated();

	public static Type mapClass(MappingTarget target, Type type) {
		if (type.getSort() != Type.OBJECT)
			return type;

		if (!obfuscated)
			return type;

		String descriptor = type.getInternalName();
		if (!descriptor.startsWith("net/minecraft/") || !descriptor.contains("/"))
			return type;
		if (!target.mappings.containsKey(descriptor))
			throw new NoClassDefFoundError("Could not find mapping for class " + descriptor);

		return Type.getObjectType(target.mappings.get(descriptor).notch);
	}

	public static String mapField(MappingTarget target, String owner, String name) {
		if (!obfuscated)
			return name;

		if (!owner.startsWith("net/minecraft/") || !owner.contains("/"))
			return name;
		if (!target.mappings.containsKey(owner))
			throw new NoClassDefFoundError("Could not find mapping for class " + owner);

		String mapping = target.mappings.get(owner).fields.get(name);
		if (mapping == null)
			throw elevate(new NoSuchFieldException(), "Could not find srg mapping for %s.%s", owner, name);
		return mapping;
	}

	public static String mapField(MappingTarget target, Class<?> owner, String name) {
		if (!obfuscated)
			return name;

		String ownerName = Type.getInternalName(owner);
		if (!ownerName.startsWith("net/minecraft/") || !ownerName.contains("/"))
			return name;

		Class<?> lookupClass = owner;
		while (lookupClass != Object.class) {
			if (target.mappings.containsKey(ownerName) && target.mappings.get(ownerName).fields.containsKey(name))
				return target.mappings.get(ownerName).fields.get(name);

			ownerName = Type.getInternalName(lookupClass = lookupClass.getSuperclass());
		}
		throw elevate(new NoSuchFieldException(), "Could not find srg mapping for %s.%s", Type.getInternalName(lookupClass), name);
	}

	public static String mapMethodName(MappingTarget target, String owner, String name, String desc) {
		owner = owner.replace('.', '/');
		if (!obfuscated)
			return name;

		if (!owner.startsWith("net/minecraft/") || !owner.contains("/"))
			return name;
		if (!target.mappings.containsKey(owner))
			throw new NoClassDefFoundError("Could not find mapping for class " + owner);

		String mapping = target.mappings.get(owner).methods.get(name + desc);
		if (mapping == null)
			throw elevate(new NoSuchMethodException(), "Could not find srg mapping for %s.%s %s", owner, name, desc);

		return mapping;
	}

	public static String mapMethodName(MappingTarget target, Class<?> owner, String name, String desc) {
		String lookupName = Type.getInternalName(owner);
		lookupName = lookupName.replace('.', '/');
		if (!obfuscated)
			return name;

		if (!lookupName.startsWith("net/minecraft/") || !lookupName.contains("/"))
			return name;

		Class<?> lookupClass = owner;
		while (lookupClass != Object.class) {
			if (target.mappings.containsKey(lookupName) && target.mappings.get(lookupName).methods.containsKey(name + desc))
				return target.mappings.get(lookupName).methods.get(name + desc);

			lookupName = Type.getInternalName(lookupClass = lookupClass.getSuperclass());
		}

		throw elevate(new NoSuchMethodException(), "Could not find srg mapping for %s.%s %s", Type.getInternalName(owner), name, desc);
	}

	public static String mapMethodDesc(MappingTarget target, String desc) {
		if (!obfuscated)
			return desc;

		Type returnType = mapClass(target, Type.getReturnType(desc));
		Type[] argumentTypes = Arrays.stream(Type.getArgumentTypes(desc))
			.map(type -> mapClass(target, type))
			.toArray(Type[]::new);

		return Type.getMethodDescriptor(returnType, argumentTypes);
	}

	private static class MappingClass {
		String notch;
		Map<String, String> methods = new HashMap<>();
		Map<String, String> fields = new HashMap<>();
	}

	public enum MappingTarget {
		NOTCH(IOUtil.gson.fromJson(new InputStreamReader(FileProvider.getData("mcp_notch_mappings.json")), new TypeToken<Map<String, MappingClass>>() {}.getType())),
		SRG(IOUtil.gson.fromJson(new InputStreamReader(FileProvider.getData("mcp_srg_mappings.json")), new TypeToken<Map<String, MappingClass>>() {}.getType()));

		private final Map<String, MappingClass> mappings;
		MappingTarget(Map<String, MappingClass> mappings) {
			this.mappings = mappings;
		}
	}

}
