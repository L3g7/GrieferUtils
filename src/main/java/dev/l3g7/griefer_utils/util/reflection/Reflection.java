package dev.l3g7.griefer_utils.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Frontend of reflection.
 */
public class Reflection {

	/**
	 * Returns the value of a field.
	 */
	public static <V> V get(Object target, String name) {
		return FieldReflection.get(target, name);
	}

	/**
	 * Sets the value of a field.
	 */
	public static void set(Object target, String name, Object value) {
		FieldReflection.set(target, name, value);
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
	 * Gets a method with matching name and parameters in the targetClass or its super classes.
	 */
	public static Method getMethod(Class<?> targetClass, String name, Object... params) {
		return MethodReflection.resolveMethod(targetClass, name, params);
	}

	/**
	 * Tries to load the class with the specified name.
	 */
	public static Class<?> load(String name) {
		return ClassReflection.load(name);
	}

}
