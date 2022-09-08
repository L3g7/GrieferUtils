package dev.l3g7.griefer_utils.util.reflection;

import java.lang.reflect.Field;

import static dev.l3g7.griefer_utils.util.Util.elevate;

/**
 * field related reflection.
 */
@SuppressWarnings("unchecked")
class FieldReflection {

	/**
	 * Returns the value of a field.
	 */
	static <V> V get(Object target, String name) {

		// Check target
		if (target == null)
			throw elevate(new IllegalArgumentException(), "Tried to get field '%s' of null", name);

		// Get field
		Class<?> targetClass = target instanceof Class<?> ? (Class<?>) target : target.getClass();
		Field field = resolveField(targetClass, name);

		// Check field
		if (field == null)
			throw elevate(new NoSuchFieldException(), "Could not find field '%s' in '%s'", name, targetClass.getName());

		// Get value
		try {
			field.setAccessible(true);
			return (V) field.get(target);
		} catch (Throwable e) {
			throw elevate(e, "Tried to access field '%s' in '%s'", name, targetClass.getName());
		}
	}

	/**
	 * Sets the value of a field.
	 */
	static void set(Object target, String name, Object value) {

		// Check target
		if (target == null)
			throw elevate(new IllegalArgumentException(), "Tried to get field '%s' of null", name);

		// Get field
		Class<?> targetClass = target instanceof Class<?> ? (Class<?>) target : target.getClass();
		Field field = resolveField(targetClass, name);

		// Check field
		if (field == null)
			throw elevate(new NoSuchFieldException(), "Could not find field '%s' in '%s'", name, targetClass.getName());

		// Get value
		try {
			field.setAccessible(true);
			field.set(target, value);
		} catch (Throwable e) {
			throw elevate(e, "Tried to access field '%s' in '%s'", name, targetClass.getName());
		}
	}

	/**
	 * Gets the field matching the fieldName in the targetClass or its super classes.
	 */
	static Field resolveField(Class<?> targetClass, String fieldName) {
		while (targetClass != null) {
			try {
				return targetClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException ignored) {
				targetClass = targetClass.getSuperclass();
			}
		}
		return null;
	}

}
