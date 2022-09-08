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
