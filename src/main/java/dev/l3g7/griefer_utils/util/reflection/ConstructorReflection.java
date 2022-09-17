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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static dev.l3g7.griefer_utils.util.Util.elevate;

/**
 * constructor related reflection.
 */
@SuppressWarnings("unchecked")
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
				return (Constructor<T>) constructor;
		}

		return null;
	}

}
