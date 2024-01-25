/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.reflection;

import dev.l3g7.griefer_utils.core.util.ArrayUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static dev.l3g7.griefer_utils.core.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.Util.elevate;

/**
 * Constructor related reflection.
 */
class ConstructorReflection {

	/**
	 * Creates a new instance of the targetClass.
	 */
	static <T> T construct(Class<T> targetClass, Object... params) {
		Constructor<T> constructor = resolveConstructor(targetClass, params);
		if(constructor == null)
			throw elevate(new NoSuchMethodException(), "Could not find constructor matching parameters in '%s'", targetClass.getName());

		// Create instance
		try {
			constructor.setAccessible(true);
			return constructor.newInstance(params);
		} catch (InvocationTargetException e) {
			throw elevate(e.getCause(), "Tried to construct '%s'", targetClass.getName());
		} catch (Throwable e) {
			throw elevate(e, "Tried to construct '%s'", targetClass.getName());
		}
	}

	/**
	 * Gets a constructor with matching parameters in the targetClass.
	 */
	private static <T> Constructor<T> resolveConstructor(Class<T> targetClass, Object[] parameters) {
		for (Constructor<?> constructor : targetClass.getDeclaredConstructors()) {
			// Check arg types
			if (ArrayUtil.equals(constructor.getParameterTypes(), parameters, ClassReflection::isApplicable))
				return c(constructor);
		}

		return null;
	}

}
