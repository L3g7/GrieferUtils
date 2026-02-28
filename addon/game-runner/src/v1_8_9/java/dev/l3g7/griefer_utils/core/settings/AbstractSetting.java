/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.functions.Function;
import dev.l3g7.griefer_utils.core.api.misc.functions.Runnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
		return getStorage().configKey;
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
			V value = get();

			// Check if value matches the fallback value
			if (s.unsetIfDefaultValue && value instanceof List<?> list ? list.isEmpty() : Objects.equals(s.fallbackValue, value))
				Config.unset(s.configKey);
			else
				Config.set(s.configKey, s.encodeFunc.apply(value));
			Config.save();
		}

		return (S) this;
	}

	/**
	 * If unset, sets the current value to the given one.
	 */
	default S defaultValue(V value) {
		getStorage().fallbackValue = value;
		return (S) this;
	}

	/**
	 * Marks the default value as being dynamic.
	 */
	default S dynamicDefaultValue() {
		getStorage().unsetIfDefaultValue = false;
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
	 * Returns the version when this setting was added.
	 */
	default UpdateInfo since() {
		return getStorage().updateInfo;
	}

	/**
	 * Sets the version when this setting was added.
	 */
	default S since(String version) {
		return since(version, null);
	}

	/**
	 * Sets the version when this setting was added, with a custom badge message.
	 */
	default S since(String version, String message) {
		return since(new UpdateInfo(this, version, message, false));
	}

	/**
	 * Sets the version when this setting was added.
	 */
	default S since(UpdateInfo updateInfo) {
		getStorage().updateInfo = updateInfo;
		return (S) this;
	}

	default void bubbleSince(Object parent) {
		if (parent instanceof AbstractSetting<?, ?> setting && since() != null)
			setting.since(since().bubble(setting));
	}

	/**
	 * A storage for a value.
	 */
	class Storage<T> {

		public T value = null;
		public T fallbackValue;
		public boolean unsetIfDefaultValue = true;

		public String configKey = null;
		private String inferredKey = null;

		public boolean subsettingConfig = true;
		public UpdateInfo updateInfo = null;
		public final List<Consumer<T>> callbacks = new ArrayList<>();

		public final Function<T, JsonElement> encodeFunc;
		public final Function<JsonElement, T> decodeFunc;

		public Storage(Function<T, JsonElement> encodeFunc, Function<JsonElement, T> decodeFunc, T fallbackValue) {
			this.encodeFunc = encodeFunc;
			this.decodeFunc = decodeFunc;
			this.fallbackValue = fallbackValue;
		}

	}

	/**
	 * A wrapper for data from {@link AbstractSetting#since(String, String)};
	 */
	record UpdateInfo(AbstractSetting<?, ?> owner, String version, String message, boolean bubbled) {
		UpdateInfo bubble(AbstractSetting<?, ?> target) {
			return new UpdateInfo(target, version, message, true);
		}

		public boolean isVisible() {
			if (bubbled) {
				for (BaseSetting<?> childSetting : owner.getChildSettings()) {
					if (childSetting instanceof AbstractSetting<?,?> setting)
						if (setting.since() != null && setting.since().isVisible())
							return true;
				}

				return false;
			}

			return !getAcknowledgedChanges().contains(new JsonPrimitive(owner.configKey()));
		}

		public void hide() {
			if (!isVisible())
				return;

			getAcknowledgedChanges().add(owner.configKey());
		}

		private static JsonArray getAcknowledgedChanges() {
			if (!Config.has("settings.acknowledged_changes"))
				Config.set("settings.acknowledged_changes", new JsonArray());

			return Config.get("settings.acknowledged_changes").getAsJsonArray();
		}

		@Override
		public String toString() {
			if (message != null)
				return message;

			if (bubbled)
				return "§9Neue Settings seit §f" + version;

			return "§9Seit " + version;
		}
	}

}
