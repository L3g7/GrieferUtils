package dev.l3g7.griefer_utils.core.api.misc;

import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;

/**
 * A lazy getter.
 */
public class Lazy<T> {

	private final Supplier<T> generator;
	private final Option<T> value = Option.empty();

	public Lazy(Supplier<T> generator) {
		this.generator = generator;
	}

	public T get() {
		if (!value.isSet())
			value.set(generator.get());

		return value.get();
	}

}
