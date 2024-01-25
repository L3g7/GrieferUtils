/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.reflection;

import org.apache.commons.lang3.ClassUtils;

import java.net.URLClassLoader;

import static dev.l3g7.griefer_utils.core.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.Util.elevate;

/**
 * Class related reflection.
 */
class ClassReflection {

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
		if (object instanceof Class<?>)
			return targetClass.isAssignableFrom((Class<?>) object);

		return (!targetClass.isPrimitive() && object == null) || targetClass.isInstance(object) || ClassUtils.primitiveToWrapper(targetClass).isInstance(object);
	}

	/**
	 * Tries to find a .class file for the given class.
	 */
	static boolean exists(String name) {
		URLClassLoader cl = ((URLClassLoader) ClassReflection.class.getClassLoader());
		return cl.findResource(name.replace('.', '/') + ".class") != null;
	}

}
