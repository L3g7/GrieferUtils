package dev.l3g7.griefer_utils.core.api.misc.primitives.containers;

import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Function;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Supplier;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A container with either its value or an exception set.
 */
public class Result<T> {

	private final T value;
	private final Throwable error;

	private Result(T value, Throwable error) {
		this.value = value;
		this.error = error;
	}

	public static <T> Result<T> ok(T value) {
		return new Result<>(value, null);
	}

	public static <T> Result<T> err(@NotNull Throwable error) {
		return new Result<>(null, error);
	}

	public static <T> Result<T> tryGet(Supplier<T> supplier) {
		try {
			return Result.ok(supplier.getWithThrowable());
		} catch (Throwable e) {
			return Result.err(e);
		}
	}

	public boolean isOk() {
		return error == null;
	}

	public boolean isErr() {
		return error != null;
	}

	/**
	 * Returns the value or elevates the stored error to RuntimeException.
	 */
	public T unwrap() {
		if (isOk())
			return value;
		else
			throw Util.elevate(error);
	}

	/**
	 * Returns the stored value or null.
	 */
	public T unwrapOrNull() {
		return value;
	}

	/**
	 * Returns the stored or the fallback value.
	 */
	public T unwrapOr(T fallback) {
		if (isOk())
			return value;
		else
			return fallback;
	}

	/**
	 * Returns the stored or the fallback value.
	 */
	public T unwrapOr(Supplier<T> fallback) {
		if (isOk())
			return value;
		else
			return fallback.get();
	}

	/**
	 * Feeds the consumer if the value is set. No-op if the Result contains an error.
	 */
	public void ifOk(Consumer<T> consumer) {
		if (isOk())
			consumer.accept(value);
	}

	/**
	 * Feeds one of the given consumers.
	 */
	public void match(Consumer<T> valueConsumer, Consumer<@NotNull Throwable> errorConsumer) {
		if (isOk())
			valueConsumer.accept(value);
		else
			errorConsumer.accept(error);
	}

	public Option<T> ok() {
		if (isOk())
			return Option.of(value);
		else
			return Option.empty();
	}

	/**
	 * Maps the value, if set. If the mapping function throws an error, an errored Result will be returned.
	 */
	public <V> Result<V> map(Function<T, @Nullable V> mapper) {
		if (isErr())
			return Reflection.c(this);

		return tryGet(() -> mapper.applyWithThrowable(value));
	}

}
