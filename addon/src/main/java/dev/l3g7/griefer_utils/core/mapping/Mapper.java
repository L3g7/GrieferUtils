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

package dev.l3g7.griefer_utils.core.mapping;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import dev.l3g7.griefer_utils.core.mapping.MappingEntries.MappedClass;
import dev.l3g7.griefer_utils.core.mapping.MappingEntries.MappedField;
import dev.l3g7.griefer_utils.core.mapping.MappingEntries.MappedList;
import dev.l3g7.griefer_utils.core.mapping.MappingEntries.MappedMethod;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.core.util.Util;
import net.labymod.core.asm.LabyModCoreMod;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.Collection;

import static dev.l3g7.griefer_utils.core.mapping.Mapping.OBFUSCATED;
import static dev.l3g7.griefer_utils.core.mapping.Mapping.UNOBFUSCATED;

public class Mapper {

	private static final MappedList<MappedClass> classes = new MappedList<>();

	public static void loadMappings(String minecraftVersion, String mappingVersion) {
		try {
			File mappings = new File(Launch.assetsDir, String.format("griefer_utils/mappings/%s_stable_%s.json", minecraftVersion, mappingVersion));
			Collection<MappedClass> mappedClasses;

			if (mappings.exists()) {
				// Load mappings from file
				// noinspection UnstableApiUsage // TypeToken is marked as @Beta
				mappedClasses = IOUtil.gson.fromJson(new FileReader(mappings), new TypeToken<Collection<MappedClass>>() {}.getType());
				if (mappedClasses.isEmpty()) {
					// Probably invalid download, overwrite
					mappedClasses = new MappingCreator().createMappings("1.8.9", "22");
					IOUtil.write(mappings, new Gson().toJson(mappedClasses));
				}
			} else {
				// Create and store mappings
				mappedClasses = new MappingCreator().createMappings("1.8.9", "22");
				IOUtil.write(mappings, new Gson().toJson(mappedClasses));
			}

			classes.addAll(mappedClasses);
			classes.create();

		} catch (IOException | GeneralSecurityException e) {
			throw Util.elevate(e, "Could not load mappings!");
		}
	}

	public static boolean isObfuscated() {
		return LabyModCoreMod.isObfuscated();
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
	 * Maps the name and descriptor of a method from the source mapping to the target mapping.
	 * @return the mapped name and descriptor, joined.
	 */
	public static String mapMethod(String owner, String name, String desc, Mapping sourceMapping, Mapping targetMapping) {
		if (targetMapping == OBFUSCATED)
			owner = mapClass(owner, OBFUSCATED, UNOBFUSCATED);

		MappedClass mappedOwner = classes.get(owner, sourceMapping);
		if (mappedOwner == null) {
			// Assume name does not need mapping as owner is not mapped, map descriptor manually
			return name + mapMethodDesc(desc, sourceMapping, targetMapping);
		}

		// Map name and descriptor
		MappedMethod method = mappedOwner.methods.get(name + desc, sourceMapping);
		if (method == null)
			// Assume method does not need mapping, map descriptor manually
			return name + mapMethodDesc(desc, sourceMapping, targetMapping);

		return method.getName(targetMapping) + method.getDesc(targetMapping);
	}

	/**
	 * Maps the descriptor of a method from the source mapping to the target mapping.
	 */
	public static String mapMethodDesc(String desc, Mapping sourceMapping, Mapping targetMapping) {
		Type returnType = mapType(Type.getReturnType(desc), sourceMapping, targetMapping);
		Type[] argumentTypes = mapTypes(Type.getArgumentTypes(desc), sourceMapping, targetMapping);
		return Type.getMethodDescriptor(returnType, argumentTypes);
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
		MappedMethod method = mappedOwner.methods.get(name + desc, sourceMapping);
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

		MappedField field = mappedOwner.fields.get(name, sourceMapping);
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