/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.reflection;

import dev.l3g7.griefer_utils.core.mapping.Mapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * The "frontend" of reflection.
 */
public class Reflection {

	static Mapping mappingTarget = Mapping.SEARGE;

	/**
	 * Sets the mapping target.
	 */
	public static void setMappingTarget(Mapping mappingTarget) {
		Reflection.mappingTarget = mappingTarget;
	}

	/**
	 * @return the value of a field.
	 */
	public static <V> V get(Object target, String name) {
		return FieldReflection.get(target, name);
	}

	/**
	 * @return the value of a field.
	 */
	public static <V> V get(Object target, Field field) {
		return FieldReflection.get(target, field);
	}

	/**
	 * Sets the value of a field.
	 */
	public static void set(Object target, Object value, String name) {
		FieldReflection.set(target, value, name);
	}

	/**
	 * @return all fields with the given annotation present.
	 */
	public static Field[] getAnnotatedFields(Class<?> targetClass, Class<? extends Annotation> annotation, boolean checkSuperClasses) {
		return FieldReflection.getAnnotatedFields(targetClass, annotation, checkSuperClasses);
	}

	/**
	 * Gets the field matching the fieldName in the targetClass or its super classes.
	 */
	public static Field getField(Class<?> targetClass, String name) {
		return FieldReflection.resolveField(targetClass, name);
	}

	/**
	 * @return all fields in a class and its super classes.
	 */
	public static Field[] getAllFields(Class<?> clazz) {
		return FieldReflection.getAllFields(clazz);
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
	 * Gets a method with matching name and parameters in the targetClass or its super classes.
	 */
	public static Method getMethod(Class<?> targetClass, String name, Object... params) {
		return MethodReflection.resolveMethod(targetClass, name, params);
	}

	/**
	 * @return all method in the given class, including inherited and private ones.
	 */
	public static Method[] getAllMethods(Class<?> targetClass) {
		return MethodReflection.getAllMethods(targetClass);
	}

	/**
	 * Tries to load the class with the specified name.
	 */
	public static <T> Class<T> load(String name) {
		return ClassReflection.load(name);
	}

	/**
	 * @return the package's parent.
	 */
	public static Package getParentPackage(Package pkg) {
		return PackageReflection.getParentPackage(pkg);
	}

	/**
	 * Converts the given object into a generic object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T c(Object o) {
		return (T) o;
	}

	/**
	 * Tries to find a .class file for the given class.
	 */
	public static boolean exists(String name) {
		return ClassReflection.exists(name);
	}

}
