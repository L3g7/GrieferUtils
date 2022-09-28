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
import java.lang.reflect.Method;

/**
 * The frontend of reflection.
 */
public class Reflection {

	/**
	 * Returns the value of a field.
	 */
	public static <V> V get(Object target, String name) {
		return FieldReflection.get(target, name);
	}

	/**
	 * Returns the value of a field.
	 */
	public static <V> V get(Object target, Field field) {
		return FieldReflection.get(target, field);
	}

	/**
	 * Sets the value of a field.
	 */
	public static void set(Object target, String name, Object value) {
		FieldReflection.set(target, name, value);
	}

	/**
	 * Returns all fields with the given annotation present.
	 */
	public static Field[] getAnnotatedFields(Class<?> targetClass, Class<? extends Annotation> annotation) {
		return FieldReflection.getAnnotatedFields(targetClass, annotation);
	}

	/**
	 * Gets the field matching the fieldName in the targetClass or its super classes.
	 */
	public static Field getField(Class<?> targetClass, String name) {
		return FieldReflection.resolveField(targetClass, name);
	}

	/**
	 * Creates a new instance of the targetClass.
	 */
	public static <T> T construct(Class<T> targetClass, Object... params) {
		return ConstructorReflection.construct(targetClass, params);
	}

	/**
	 * Invoke a method with given parameters.
	 */
	public static <T> T invoke(Object target, String name, Object... params) {
		return MethodReflection.invoke(target, name, params);
	}

	/**
	 * Invoke a method with given parameters.
	 */
	public static <T> T invoke(Object target, Method method, Object... params) {
		return MethodReflection.invoke(target, method, params);
	}

	/**
	 * Returns all methods with the given annotation present.
	 */
	public static Method[] getAnnotatedMethods(Class<?> targetClass, Class<? extends Annotation> annotation) {
		return MethodReflection.getAnnotatedMethods(targetClass, annotation);
	}

	/**
	 * Gets a method with matching name and parameters in the targetClass or its super classes.
	 */
	public static Method getMethod(Class<?> targetClass, String name, Object... params) {
		return MethodReflection.resolveMethod(targetClass, name, params);
	}

	/**
	 * Tries to load the class with the specified name.
	 */
	public static <T> Class<T> load(String name) {
		return ClassReflection.load(name);
	}

	/**
	 * Converts the given object into a generic object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T c(Object o) {
		return (T) o;
	}

}
