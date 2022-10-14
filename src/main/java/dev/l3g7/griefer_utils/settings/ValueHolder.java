/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	/**
	 * @return the currently held value.
	 */
	default V get() {
		Storage<V> storage = getStorage();

		// Use fallback value if value is null
		if (storage.value == null)
			return storage.fallbackValue;

		return storage.value;
	}

	/**
	 * Sets the currently held value.
	 */
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

	/**
	 * Sets a fallback value to use if no value is in the config and no default value is set.
	 */
	default S fallbackValue(V value) {
		Storage<V> s = getStorage();
		s.fallbackValue = value;
		return (S) this;
	}

	/**
	 * Loads the value from the given config key and adds a callback that saves changes to it.
	 */
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

	/**
	 * If unset, sets the current value to the given one.
	 */
	default S defaultValue(V value) {
		if (get() == null)
			set(value);

		return (S) this;
	}

	/**
	 * Adds a callback to be triggered when the held value changes.
	 */
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
		private T fallbackValue = null;
		private final List<Consumer<T>> callbacks = new ArrayList<>();

		private final Function<T, JsonElement> encodeFunc;
		private final Function<JsonElement, T> decodeFunc;

		public Storage(Function<T, JsonElement> encodeFunc, Function<JsonElement, T> decodeFunc) {
			this.encodeFunc = encodeFunc;
			this.decodeFunc = decodeFunc;
		}

	}
}
