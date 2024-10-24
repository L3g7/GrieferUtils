/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.functions;

import dev.l3g7.griefer_utils.core.api.util.Util;

/**
 * Like {@link java.util.function.Function}, but able to throw exceptions.
 */
@FunctionalInterface
public interface Function<T, R> extends java.util.function.Function<T, R> {

    R applyWithThrowable(T t) throws Throwable;

	default R apply(T t) {
		try {
			return applyWithThrowable(t);
		} catch (Throwable e) {
			throw Util.elevate(e);
		}
	}

}