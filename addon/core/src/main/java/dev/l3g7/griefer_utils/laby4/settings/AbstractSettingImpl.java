/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.settings;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.api.misc.functions.Function;
import dev.l3g7.griefer_utils.settings.AbstractSetting;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.SettingElement;

import java.util.UUID;

/**
 * A superclass for all settings directly based on SettingElement.
 */
public abstract class AbstractSettingImpl<S extends AbstractSetting<S, V>, V> extends SettingElement implements BaseSettingImpl<S, V> {

	private final ExtendedStorage<V> storage;

	public AbstractSettingImpl(Function<V, JsonElement> encodeFunc, Function<JsonElement, V> decodeFunc, V fallbackValue) {
		super(UUID.randomUUID().toString(), null, null, new String[0]);
		storage = new ExtendedStorage<>(encodeFunc, decodeFunc, fallbackValue);
		init();
	}

	@Override
	public ExtendedStorage<V> getStorage() {
		return storage;
	}

	@Override
	public Component displayName() {
		return Component.text(name());
	}

	@Override
	public Component getDescription() {
		String description = getStorage().description;
		return description == null ? null : Component.text(description);
	}

	@Override
	public Icon getIcon() {
		return getStorage().icon;
	}

	protected abstract Widget[] createWidgets();

	public void create(Config config, Setting parent) {
		if (getWidgets() == null)
			setWidgets(createWidgets());

		BaseSettingImpl.super.create(config, parent);
	}

}
