/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.primitives.containers;

import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Supplier;

/**
 * A lazy getter.
 */
public class Lazy<T> {

	private final Supplier<T> generator;
	private final Option<T> value = Option.emptyMut();

	public Lazy(Supplier<T> generator) {
		this.generator = generator;
	}

	public T get() {
		if (!value.isSet())
			value.set(generator.get());

		return value.get();
	}

}
