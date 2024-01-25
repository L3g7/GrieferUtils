/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.token_providers;

import java.io.IOException;

public interface TokenProvider {

	boolean loadWithException() throws IOException;

	default boolean load() {
		try {
			return loadWithException();
		} catch (Exception e) {
			return false;
		}
	}

}
