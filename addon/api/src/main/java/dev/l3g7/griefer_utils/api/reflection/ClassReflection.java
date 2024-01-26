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

package dev.l3g7.griefer_utils.api.reflection;

import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.api.bridges.MinecraftBridge.minecraftBridge;
import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.api.util.Util.elevate;

/**
 * Class related reflection.
 */
class ClassReflection {

	private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();

	static {
		primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
		primitiveWrapperMap.put(Byte.TYPE, Byte.class);
		primitiveWrapperMap.put(Character.TYPE, Character.class);
		primitiveWrapperMap.put(Short.TYPE, Short.class);
		primitiveWrapperMap.put(Integer.TYPE, Integer.class);
		primitiveWrapperMap.put(Long.TYPE, Long.class);
		primitiveWrapperMap.put(Double.TYPE, Double.class);
		primitiveWrapperMap.put(Float.TYPE, Float.class);
		primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
	}

	/**
	 * Loads the class.
	 */
	static <T> Class<T> load(String name) {
		try {
			return c(Class.forName(name.replaceAll("\\.class$", "").replace('/', '.')));
		} catch (Throwable e) {
			throw elevate(e, "Could not load class '%s'", name);
		}
	}

	/**
	 * Checks if the object can be passed as targetClass.
	 */
	static boolean isApplicable(Class<?> targetClass, Object object) {
		if (object instanceof Class<?> && targetClass != Class.class)
			return targetClass.isAssignableFrom((Class<?>) object);

		return (!targetClass.isPrimitive() && object == null) || targetClass.isInstance(object) || primitiveWrapperMap.getOrDefault(targetClass, targetClass).isInstance(object);
	}

	/**
	 * Tries to find a .class file for the given class.
	 */
	static boolean exists(String name) {
		name = name.replace('.', '/') + ".class";
		return ClassReflection.class.getClassLoader().getResource(name) != null
			|| minecraftBridge.launchClassLoader().getResource(name) != null;
	}

}
