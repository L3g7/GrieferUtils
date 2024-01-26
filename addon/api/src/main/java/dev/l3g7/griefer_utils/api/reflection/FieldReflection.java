/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.api.reflection;

import dev.l3g7.griefer_utils.api.mapping.Mapper;
import dev.l3g7.griefer_utils.api.util.ArrayUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.l3g7.griefer_utils.api.mapping.Mapping.UNOBFUSCATED;
import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.api.util.Util.elevate;

/**
 * Field related reflection.
 */
class FieldReflection {

	/**
	 * @return the value of a field.
	 */
	static <V> V get(Object target, String name) {
		return get(target, getField(target, name));
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
	static void set(Object target, Object value, String name) {
		try {
			Field field = getField(target, name);
			field.setAccessible(true);
			field.set(target, value);
		} catch (Throwable e) {
			Class<?> targetClass = target instanceof Class<?> ? (Class<?>) target : target.getClass();
			throw elevate(e, "Tried to access field '%s' in '%s'", name, targetClass.getName());
		}
	}

	private static Field getField(Object target, String name) {
		Field field = resolveField(target, name);

		// Check field
		if (field == null) {
			Class<?> targetClass = target instanceof Class<?> ? (Class<?>) target : target.getClass();
			throw elevate(new NoSuchFieldException(), "Could not find field '%s' in '%s'", name, targetClass.getName());
		}

		return field;
	}

	/**
	 * Gets the field matching the fieldName or its obfuscated equivalent in the targetClass or its super classes.
	 */
	static Field resolveField(Object target, String name) {
		// Check target
		if (target == null)
			throw elevate(new IllegalArgumentException(), "Tried to get field '%s' of null", name);

		Class<?> targetClass = target instanceof Class<?> ? (Class<?>) target : target.getClass();

		// Get field
		Field field = null;
		Class<?> currentClass = targetClass;
		while (currentClass != null) {
			try {
				String lookupName = name;

				// Map name
				if (Mapper.isObfuscated())
					lookupName = Mapper.mapField(currentClass, name, UNOBFUSCATED, Reflection.mappingTarget);

				// Lookup field
				field = currentClass.getDeclaredField(lookupName);
				break;
			} catch (NoSuchFieldException ignored) {
				currentClass = currentClass.getSuperclass();
			}
		}

		return field;
	}

	/**
	 * @return all fields with the given annotation present.
	 */
	static Field[] getAnnotatedFields(Class<?> targetClass, Class<? extends Annotation> annotation, boolean checkSuperClasses) {
		List<Field> fields = new ArrayList<>();
		for (Field field : checkSuperClasses ? getAllFields(targetClass) : ArrayUtil.flatmap(Field.class, targetClass.getDeclaredFields(), targetClass.getFields())) {
			if (field.isAnnotationPresent(annotation))
				fields.add(field);
		}

		return fields.toArray(new Field[0]);
	}

	/**
	 * @return all fields in a class and its super classes.
	 */
	static Field[] getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		do {
			fields.addAll(Arrays.asList(ArrayUtil.flatmap(Field.class, clazz.getDeclaredFields(), clazz.getFields())));
		} while ((clazz = clazz.getSuperclass()) != null);

		return fields.toArray(new Field[0]);
	}

}
