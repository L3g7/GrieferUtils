/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.file_provider.meta;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.Util.elevate;

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
			if (isEnum && value.getClass().isArray()) {
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
		if (isEnum && value.getClass().isArray())
			value = convertToEnum(value);
		values.put(key, value);
		return c(value);
	}

	/**
	 * Converts a String[2] into an Enum.
	 *
	 * @see AnnotationNode#values
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private Object convertToEnum(Object value) {
		String[] data = (String[]) value;
		return Enum.valueOf((Class<? extends Enum>) c(FileProvider.getClassMetaByDesc(data[0]).load()), data[1]);
	}

	@Override
	public String toString() {
		return desc;
	}

}
