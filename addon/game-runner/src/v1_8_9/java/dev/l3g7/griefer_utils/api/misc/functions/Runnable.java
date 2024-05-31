/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.api.misc.functions;

import dev.l3g7.griefer_utils.api.util.Util;

/**
 * Like {@link java.lang.Runnable}, but able to throw exceptions.
 */
@FunctionalInterface
public interface Runnable extends java.lang.Runnable {

    void runWithException() throws Exception;

	default void run() {
		try {
			runWithException();
		} catch (Exception e) {
			throw Util.elevate(e);
		}
	}

}