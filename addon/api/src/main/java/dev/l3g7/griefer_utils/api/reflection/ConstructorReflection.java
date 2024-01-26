/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.api.reflection;

import dev.l3g7.griefer_utils.api.util.ArrayUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.api.util.Util.elevate;

/**
 * Constructor related reflection.
 */
class ConstructorReflection {

	/**
	 * Creates a new instance of the targetClass.
	 */
	static <T> T construct(Class<T> targetClass, Object... params) {
		Constructor<T> constructor = resolveConstructor(targetClass, params);
		if (constructor == null)
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
			else if (targetClass.getSimpleName().endsWith("DefaultSubscribeMethod")) {
				Class<?>[] a = constructor.getParameterTypes();
				Object[] b = parameters;
				if(a.length != b.length)
					System.err.println(a.length + " isn't applicable to " + b.length);

				else
					for(int i = 0; i < a.length; i++)
						if(!ClassReflection.isApplicable(a[i], b[i]))
							System.err.println(a[i] + " isn't applicable to " + b[i]);
			}
		}

		if (targetClass.getSimpleName().endsWith("DefaultSubscribeMethod"))
			System.err.println("Checked " + Arrays.toString(targetClass.getDeclaredConstructors()) +  " constructors");
		else
			System.err.println("asdkasd " + targetClass.getSimpleName());
		return null;
	}

}
