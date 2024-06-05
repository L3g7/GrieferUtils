/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.file_provider.meta;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.api.util.Util.elevate;

/**
 * Meta information of an annotation.
 */
public class AnnotationMeta implements Opcodes {

	public final String desc;
	private final Map<String, Object> values = new HashMap<>();
	private final Object annotationInstance;
	private Class<?> type;

	/**
	 * Load the information from ASM's {@link AnnotationNode}.
	 */
	public AnnotationMeta(AnnotationNode data) {
		this.desc = data.desc;

		// Load values
		if (data.values != null) {
			Iterator<Object> it = data.values.iterator();
			while (it.hasNext())
				this.values.put((String) it.next(), it.next());
		}

		this.annotationInstance = null;
		this.type = null;
	}

	/**
	 * Load the information from Reflection's {@link Annotation}.
	 */
	public AnnotationMeta(Annotation annotation) {
		this.desc = Type.getInternalName(annotation.annotationType());
		this.annotationInstance = annotation;
		this.type = annotation.getClass();
	}


	/**
	 * Loads the annotation class.
	 */
	public <T> Class<T> loadClass() {
		if (type != null)
			return c(type);
		else
			return c(type = FileProvider.getClassMetaByDesc(desc).load());
	}

	/**
	 * @return the defined or default value.
	 */
	public <T> T getValue(String key, boolean isEnum) {
		// Check if value is known
		if (values.containsKey(key)) {
			Object value = values.get(key);

			// Check for enums
			if (isEnum && mustConvertToEnum(value.getClass())) {
				value = convertToEnum(value);
				values.put(key, value);
			}
			return c(value);
		}

		// Load value using Reflection
		Class<? extends Annotation> annotationClass = loadClass();
		Method getter = Reflection.getMethod(annotationClass, key);

		Object value = null;

		// Get defined value
		if (annotationInstance != null)
			value = Reflection.invoke(annotationInstance, getter);

		// Get default value
		if (value == null)
			value = getter.getDefaultValue();

		if (value == null)
			throw elevate(new NullPointerException(), "Could not get value of %s", desc);

		// Check for enums
		if (isEnum && mustConvertToEnum(value.getClass()))
			value = convertToEnum(value);
		values.put(key, value);
		return c(value);
	}

	private boolean mustConvertToEnum(Class<?> clazz) {
		if (clazz == ArrayList.class)
			return true;

		if (!clazz.isArray())
			return false;

		return clazz.getComponentType() == String.class;
	}

	/**
	 * Converts a String[2] into an Enum and an ArrayList into an array of Enums.
	 *
	 * @see AnnotationNode#values
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private <T extends Enum<T>> Object convertToEnum(Object value) {
		if (value.getClass().isArray()) {
			String[] data = (String[]) value;
			return Enum.valueOf((Class<? extends Enum>) c(FileProvider.getClassMetaByDesc(data[0]).load()), data[1]);
		}

		List<String[]> list = (List<String[]>) value;
		T[] result = null;

		for (int i = 0; i < list.size(); i++) {
			String[] data = list.get(i);
			Class<T> clazz = c(FileProvider.getClassMetaByDesc(data[0]).load());
			if (result == null)
				result = c(Array.newInstance(clazz, list.size()));

			result[i] = Enum.valueOf(clazz, data[1]);
		}

		return result;
	}

	@Override
	public String toString() {
		return desc;
	}

}
