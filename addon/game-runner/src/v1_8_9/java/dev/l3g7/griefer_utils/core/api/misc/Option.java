package dev.l3g7.griefer_utils.core.api.misc;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

/**
 * A nullable mutable Optional.
 */
public class Option<T> {

	private static final Option<?> EMPTY = new Option<>(null, false, false);

	private T value;
	private boolean isSet;
	private final boolean mutable;

	private Option(T value, boolean isSet, boolean mutable) {
		this.value = value;
		this.isSet = isSet;
		this.mutable = mutable;
	}

	public static <T> Option<T> of(T value) {
		return new Option<>(value, true, true);
	}

	public static <T> Option<T> empty() {
		return c(EMPTY);
	}

	public static <T> Option<T> emptyMut() {
		return new Option<>(null, false, true);
	}

	/**
	 * @return the contained value, or null if empty.
	 */
	public T get() {
		return value;
	}

	public T getOr(T fallback) {
		return isSet ? value : fallback;
	}

	public void set(T value) {
		if (!mutable)
			throw new IllegalStateException("Cannot change immutable options");

		this.value = value;
		isSet = true;
	}

	public boolean isSet() {
		return isSet;
	}

	public boolean isUnset() {
		return !isSet;
	}

	@Override
	public String toString() {
		if (!isSet)
			return "Empty";

		return "Some(" + value + ")";
	}
}
