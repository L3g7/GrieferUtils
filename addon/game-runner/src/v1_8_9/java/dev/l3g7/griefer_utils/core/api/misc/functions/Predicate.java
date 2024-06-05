/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.functions;

import dev.l3g7.griefer_utils.core.api.util.Util;

/**
 * Like {@link java.util.function.Predicate}, but able to throw exceptions.
 */
@FunctionalInterface
public interface Predicate<T> extends java.util.function.Predicate<T> {

	boolean testWithException(T t) throws Exception;

	default boolean test(T t) {
		try {
			return testWithException(t);
		} catch (Exception e) {
			throw Util.elevate(e);
		}
	}

	static <T> Predicate<T> all(Predicate<T>[] predicates) {
		return t -> {
			for (Predicate<T> predicate : predicates)
				if (!predicate.test(t))
					return false;

			return true;
		};
	}

}
