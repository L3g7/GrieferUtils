package dev.l3g7.griefer_utils.settings;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.util.reflection.Reflection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An interface for objects with modifiable values.
 *
 * @param <S> The implementation class
 * @param <V> The value type
 */
@SuppressWarnings("unchecked")
public interface ValueHolder<S extends ValueHolder<S, V>, V> {

	Storage<V> getStorage();

	default V get() {
		return getStorage().value;
	}

	default S set(V value) {
		Storage<V> s = getStorage();
		s.value = value;

		// Update element value
		if (Reflection.getField(getClass(), "currentValue") != null)
			Reflection.set(this, "currentValue", value);
		if (Reflection.getMethod(getClass(), "updateValue") != null)
			Reflection.invoke(this, "updateValue");

		// Trigger callbacks
		s.callbacks.forEach(c -> c.accept(value));

		return (S) this;
	}

	default S config(String configKey) {
		Storage<V> s = getStorage();
		s.configKey = configKey;

		// Load value
		if (Config.has(configKey))
			set(s.decodeFunc.apply(Config.get(configKey)));

		// Add callback
		s.callbacks.add(value -> {
			Config.set(s.configKey, s.encodeFunc.apply(value));
			Config.save();
		});

		return (S) this;
	}

	default S defaultValue(V value) {
		if (getStorage().value == null)
			getStorage().value = value;

		return (S) this;
	}

	default S callback(Consumer<V> callback) {
		getStorage().callbacks.add(callback);
		return (S) this;
	}

	/**
	 * A storage for a value.
	 */
	class Storage<T> {

		private T value = null;
		private String configKey = null;
		private final List<Consumer<T>> callbacks = new ArrayList<>();

		private final Function<T, JsonElement> encodeFunc;
		private final Function<JsonElement, T> decodeFunc;

		public Storage(Function<T, JsonElement> encodeFunc, Function<JsonElement, T> decodeFunc) {
			this.encodeFunc = encodeFunc;
			this.decodeFunc = decodeFunc;
		}

	}
}
