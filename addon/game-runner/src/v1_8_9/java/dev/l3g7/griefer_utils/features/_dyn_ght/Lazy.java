package dev.l3g7.griefer_utils.features._dyn_ght;

import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;

public class Lazy<T> {
	private T value;
	private boolean inited = false;
	private final Supplier<T> callback;

	public Lazy(Supplier<T> callback) {
		this.callback = callback;
	}

	public T get() {
		if(!inited) {
			inited = true;
			value = callback.get();
		}
		return value;
	}
}
