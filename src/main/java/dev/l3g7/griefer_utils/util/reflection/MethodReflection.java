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

import dev.l3g7.griefer_utils.util.ArrayUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.Util.elevate;
import static dev.l3g7.griefer_utils.util.reflection.Reflection.c;

/**
 * Method related reflection.
 */
class MethodReflection {

	/**
	 * Invoke a method with given parameters.
	 */
	static <V> V invoke(Object target, String name, Object... params) {

		// Check target
		if (target == null)
			throw elevate(new IllegalArgumentException(), "Tried to invoke null");

		// Get field
		Class<?> targetClass = target instanceof Class<?> ? (Class<?>) target : target.getClass();

		Method method = resolveMethod(targetClass, name, params);
		if (method == null)
			throw elevate(new NoSuchMethodException(), "Could not find method '%s' with parameters '%s' in '%s'", name, ArrayUtil.toString(params, o -> o.getClass().toString(), ", "), targetClass.getName());

		return invoke(target, method, params);
	}

	/**
	 * Invoke a method with given parameters.
	 */
	static <V> V invoke(Object target, Method method, Object... params) {
		if (method == null)
			throw elevate(new IllegalArgumentException(), "Tried to invoke null method");

		// Invoke
		try {
			method.setAccessible(true);
			return c(method.invoke(target, params));
		} catch (Throwable e) {

			throw elevate(e, "Tried to invoke method '%s' with parameters '%s' in '%s'", method.getName(), ArrayUtil.toString(params, o -> o == null ? "<null>" : o.getClass().toString(), ", "), target == null ? "<null>" : target.toString());
		}
	}

	/**
	 * Gets a method with matching name and parameters in the targetClass or its super classes.
	 */
	static Method resolveMethod(Class<?> targetClass, String name, Object[] parameters) {
		for (Method method : targetClass.getDeclaredMethods()) {
			if (method.getName().equalsIgnoreCase(name) &&
				ArrayUtil.equals(method.getParameterTypes(), parameters, ClassReflection::isApplicable))
				return method;
		}

		if (targetClass.getSuperclass() != null)
			return resolveMethod(targetClass.getSuperclass(), name, parameters);

		return null;
	}

	/**
	 * @return all methods with the given annotation present.
	 */
	static Method[] getAnnotatedMethods(Class<?> targetClass, Class<? extends Annotation> annotation) {
		List<Method> methods = new ArrayList<>();
		for (Method Method : ArrayUtil.flatmap(Method.class, targetClass.getDeclaredMethods(), targetClass.getMethods())) {
			if (Method.isAnnotationPresent(annotation))
				methods.add(Method);
		}

		return methods.toArray(new Method[0]);
	}

}
