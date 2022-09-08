package dev.l3g7.griefer_utils.util.reflection;

import dev.l3g7.griefer_utils.util.ArrayUtil;

import java.lang.reflect.Constructor;

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
