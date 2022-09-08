package dev.l3g7.griefer_utils.util.reflection;

import dev.l3g7.griefer_utils.util.ArrayUtil;

import java.lang.reflect.Method;

import static dev.l3g7.griefer_utils.util.Util.elevate;

/**
 * Method related reflection.
 */
@SuppressWarnings("unchecked")
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

		// Invoke
		try {
			method.setAccessible(true);
			return (V) method.invoke(target, params);
		} catch (Throwable e) {
			throw elevate(e, "Tried to invoke method '%s' with parameters '%s' in '%s'", name, ArrayUtil.toString(params, o -> o.getClass().toString(), ", "), targetClass.getName());
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

}
