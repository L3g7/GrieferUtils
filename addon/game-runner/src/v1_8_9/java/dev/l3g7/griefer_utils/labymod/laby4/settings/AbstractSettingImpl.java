/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.misc.functions.Function;
import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.SettingElement;
import net.labymod.api.util.KeyValue;

import java.util.UUID;

/**
 * A superclass for all settings directly based on SettingElement.
 */
public abstract class AbstractSettingImpl<S extends AbstractSetting<S, V>, V> extends SettingElement implements Laby4Setting<S, V> {

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

	@Override
	public void create(Object parent) {
		if (getWidgets() == null)
			setWidgets(createWidgets());

		Laby4Setting.super.create(parent);
		for (KeyValue<Setting> element : getElements()) {
			Setting child = element.getValue();
			if (child instanceof AbstractSettingImpl<?,?> s)
				s.create(this);
		}
	}

}
