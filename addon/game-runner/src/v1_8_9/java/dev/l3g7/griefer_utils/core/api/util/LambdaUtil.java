package dev.l3g7.griefer_utils.core.api.util;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static java.lang.invoke.MethodType.methodType;

public class LambdaUtil {

	private static final Lookup globalLookup = MethodHandles.lookup();

	/**
	 * Creates a functional interface targeting the given method.
	 */
	public static <T, R extends T> R createFunctionalInterface(Class<T> functionalInterface, Method implementation, Object instance) {
		try {
			boolean isStatic = Modifier.isStatic(implementation.getModifiers());
			boolean isClass = instance instanceof Class<?>;

			if (isClass && !isStatic)
				throw new IllegalArgumentException("Cannot generate a non-static method from a class!");

			implementation.setAccessible(true);

			// Create lookup searching in target
			Lookup lookup;
			if (LABY_4.isActive()) {
				lookup = MethodHandles.privateLookupIn(implementation.getDeclaringClass(), globalLookup);
			} else {
				Reflection.set(globalLookup, "allowedModes", -1);
				lookup = globalLookup.in(implementation.getDeclaringClass());
			}

			// Create generator
			MethodType generatorType = isStatic
				? methodType(functionalInterface)
				: methodType(functionalInterface, implementation.getDeclaringClass());

			MethodHandle handle = createGenerator(implementation, lookup, generatorType, functionalInterface);

			// Create interface
			return c(isStatic ? handle.invoke() : handle.invoke(instance));
		} catch (Throwable t) {
			throw Util.elevate(t);
		}
	}

	/**
	 * Creates a {@link MethodHandle} which generates a functional interface out of the given method.
	 */
	private static MethodHandle createGenerator(Method implementation, Lookup lookup, MethodType generatorType, Class<?> functionalInterface) throws Exception {

		// The method to be implemented
		Method functionalMethod = getFunctionalMethod(functionalInterface);
		MethodType generatedMethodType = methodType(functionalMethod.getReturnType(), functionalMethod.getParameterTypes());

		// The method implementing the interface
		MethodHandle implementingMethod = lookup.unreflect(implementation);
		MethodType typeOfImplementingMethod = methodType(implementation.getReturnType(), implementation.getParameterTypes());

		return LambdaMetafactory.metafactory(
			lookup,
			functionalMethod.getName(), // The method created by the generator
			generatorType, // The descriptor of the generating method
			generatedMethodType, // The descriptor of the generated method
			implementingMethod, typeOfImplementingMethod // The implementation of the generated method
		).getTarget();
	}

	/**
	 * @return The abstract method of a {@link FunctionalInterface}.
	 */
	private static Method getFunctionalMethod(Class<?> functionalInterface) throws IllegalArgumentException {
		Method method = null;

		for (Method declaredMethod : functionalInterface.getDeclaredMethods()) {
			if (!Modifier.isAbstract(declaredMethod.getModifiers()))
				continue;

			if (method != null)
				// Class has more than one abstract method
				throw new IllegalArgumentException("Class " + functionalInterface.getName() + " is not a functional interface!");

			method = declaredMethod;
		}

		if (method == null)
			// Class has no abstract method
			throw new IllegalArgumentException("Class " + functionalInterface.getName() + " is not a functional interface!");

		return method;
	}

}
