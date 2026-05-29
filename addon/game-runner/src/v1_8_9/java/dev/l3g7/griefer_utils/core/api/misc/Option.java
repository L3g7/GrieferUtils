package dev.l3g7.griefer_utils.core.api.misc;

/**
 * A nullable mutable Optional.
 */
public class Option<T> {

	private T value;
	private boolean isSet;

	private Option(T value, boolean isSet) {
		this.value = value;
		this.isSet = isSet;
	}

	public static <T> Option<T> empty() {
		return new Option<>(null, false);
	}

	/**
	 * @return the contained value, or null if empty.
	 */
	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
		isSet = true;
	}

	public boolean isSet() {
		return isSet;
	}

}
