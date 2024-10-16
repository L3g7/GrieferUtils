/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.functions.Function;
import dev.l3g7.griefer_utils.core.api.misc.functions.Runnable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public interface AbstractSetting<S extends AbstractSetting<S, V>, V> extends BaseSetting<S> { // NOTE: add untyped version

	Storage<V> getStorage();

	/**
	 * Sets whether the setting is enabled.
	 * If not, it's grayed out and interactions are suppressed.
	 */
	S enabled(boolean enabled);
	default S enable() {return enabled(true);}
	default S disable() {return enabled(false);}

	/**
	 * Increases the widget size (LabyMod 4 only).
	 */
	S extend();

	/**
	 * @return the currently stored value.
	 */
	default V get() {
		Storage<V> storage = getStorage();

		// Use fallback value if value is null
		if (storage.value == null)
			return storage.fallbackValue;

		return storage.value;
	}

	/**
	 * Sets the currently stored value.
	 */
	default S set(V value) {
		Storage<V> s = getStorage();
		s.value = value;

		notifyChange();
		return (S) this;
	}

	/**
	 * Triggers all callbacks with the stored value.
	 */
	default void notifyChange() {
		Storage<V> s = getStorage();
		s.callbacks.forEach(c -> c.accept(get()));
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
		s.callbacks.add(value -> save());

		for (BaseSetting<?> setting : getChildSettings()) {
			if (setting instanceof AbstractSetting<?,?> abs) {
				if (abs.getStorage().inferredKey != null)
					// Uses the parent key as base. Technically wrong but required to preserve config compatibility
					abs.config(configKey.replaceFirst("\\.[^.]+$", "") + "." + abs.getStorage().inferredKey);
			}
		}

		return (S) this;
	}

	/**
	 * Infers the config key by joining it with the config key of the parent setting.
	 */
	default S inferConfig(String partialKey) {
		getStorage().inferredKey = partialKey;
		return (S) this;
	}

	default S disableSubsettingConfig() {
		getStorage().subsettingConfig = false;
		return (S) this;
	}

	default String configKey() {
		return getStorage().configKeySupplierTEMP.apply(this);
	}

	@Override
	default void setParent(BaseSetting<?> parent) {
		String inferredKey = getStorage().inferredKey;
		if (inferredKey == null)
			return;

		if (!(parent instanceof AbstractSetting<?,?>))
			throw new IllegalStateException("Cannot infer config key");

	}

	/**
	 * Saves the value in the config.
	 */
	default S save() {
		Storage<V> s = getStorage();

		if (s.configKey != null) {
			Config.set(s.configKey, s.encodeFunc.apply(get()));
			Config.save();
		}

		return (S) this;
	}

	/**
	 * If unset, sets the current value to the given one.
	 */
	default S defaultValue(V value) {
		if (getStorage().value == null)
			set(value);

		return (S) this;
	}

	/**
	 * Adds a callback to be triggered when the stored value changes.
	 */
	default S callback(Consumer<V> callback) {
		getStorage().callbacks.add(callback);
		return (S) this;
	}

	/**
	 * Adds a callback to be triggered when the stored value changes.
	 */
	default S callback(Runnable callback) {
		return callback(v -> callback.run());
	}

	/**
	 * A storage for a value.
	 */
	class Storage<T> {

		public T value = null;
		public String configKey = null;
		private String inferredKey = null;
		public boolean subsettingConfig = true;
		public final T fallbackValue;
		public final List<Consumer<T>> callbacks = new ArrayList<>();
		public Function<AbstractSetting<?, ?>, String> configKeySupplierTEMP = e -> e.getStorage().configKey; // TODO remove

		public final Function<T, JsonElement> encodeFunc;
		public final Function<JsonElement, T> decodeFunc;

		public Storage(Function<T, JsonElement> encodeFunc, Function<JsonElement, T> decodeFunc, T fallbackValue) {
			this.encodeFunc = encodeFunc;
			this.decodeFunc = decodeFunc;
			this.fallbackValue = fallbackValue;
		}

	}

}
