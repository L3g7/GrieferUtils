/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.util.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.Util.elevate;
import static dev.l3g7.griefer_utils.util.reflection.Reflection.c;

/**
 * Field related reflection.
 */
class FieldReflection {

	/**
	 * @return the value of a field.
	 */
	static <V> V get(Object target, String name) {

		// Check target
		if (target == null)
			throw elevate(new IllegalArgumentException(), "Tried to get field '%s' of null", name);

		// Get field
		Class<?> targetClass = target instanceof Class<?> ? (Class<?>) target : target.getClass();
		Field field = resolveField(targetClass, name);

		// Check field
		if (field == null)
			throw elevate(new NoSuchFieldException(), "Could not find field '%s' in '%s'", name, targetClass.getName());

		return get(target, field);
	}

	/**
	 * @return the value of a field.
	 */
	static <V> V get(Object target, Field field) {

		// Check target
		if (target == null)
			throw elevate(new IllegalArgumentException(), "Tried to get null field");

		// Get value
		try {
			field.setAccessible(true);
			return c(field.get(target));
		} catch (Throwable e) {
			throw elevate(e, "Tried to access field '%s' in '%s'", field.getName(), target.toString());
		}
	}

	/**
	 * Sets the value of a field.
	 */
	static void set(Object target, String name, Object value) {

		// Check target
		if (target == null)
			throw elevate(new IllegalArgumentException(), "Tried to get field '%s' of null", name);

		// Get field
		Class<?> targetClass = target instanceof Class<?> ? (Class<?>) target : target.getClass();
		Field field = resolveField(targetClass, name);

		// Check field
		if (field == null)
			throw elevate(new NoSuchFieldException(), "Could not find field '%s' in '%s'", name, targetClass.getName());

		// Get value
		try {
			field.setAccessible(true);
			field.set(target, value);
		} catch (Throwable e) {
			throw elevate(e, "Tried to access field '%s' in '%s'", name, targetClass.getName());
		}
	}

	/**
	 * Gets the field matching the fieldName in the targetClass or its super classes.
	 */
	static Field resolveField(Class<?> targetClass, String fieldName) {
		while (targetClass != null) {
			try {
				return targetClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException ignored) {
				targetClass = targetClass.getSuperclass();
			}
		}
		return null;
	}

	/**
	 * @return all fields with the given annotation present.
	 */
	static Field[] getAnnotatedFields(Class<?> targetClass, Class<? extends Annotation> annotation) {
		List<Field> fields = new ArrayList<>();
		for (Field field : targetClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(annotation))
				fields.add(field);
		}

		return fields.toArray(new Field[0]);
	}

}
