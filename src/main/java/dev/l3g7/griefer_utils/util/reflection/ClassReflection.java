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

import org.apache.commons.lang3.ClassUtils;

import static dev.l3g7.griefer_utils.util.Util.elevate;

/**
 * Class related reflection.
 */
@SuppressWarnings("unchecked")
class ClassReflection {

	static <T> Class<T> load(String name) {
		try {
			return (Class<T>) Class.forName(name);
		} catch (Throwable e) {
			throw elevate(e, "Could not load class '%s'", name);
		}
	}

	/**
	 * Checks if the object can be passed as targetClass.
	 */
	static boolean isApplicable(Class<?> targetClass, Object object) {
		return (!targetClass.isPrimitive() && object == null) || targetClass.isInstance(object) || ClassUtils.primitiveToWrapper(targetClass).isInstance(object);
	}

}
