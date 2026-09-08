/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.utils.Consumer;

public class SwitchSettingImpl extends BooleanElement implements Laby3Setting<SwitchSetting, Boolean>, SwitchSetting {

	private final ExtendedStorage<Boolean> storage = new ExtendedStorage<>(JsonPrimitive::new, JsonElement::getAsBoolean, false);

	public SwitchSettingImpl() {
		super("§cNo name set", null, v -> {}, false);
		custom("An", "Aus");
		setSettingEnabled(true);
		Reflection.set(this, "toggleListener", (Consumer<Boolean>) this::set);
	}

	@Override
	public void init() {
		super.init();
		Reflection.set(this, "currentValue", get());
		callback(v -> Reflection.set(this, "currentValue", v));
	}

	@Override
	public int getHotkeySettingOffset() {
		return 4;
	}

	@Override
	public SwitchSetting addHotkeySetting(String whatActivates, TriggerMode defaultTriggerMode) {
		if (getSubSettings().getElements().isEmpty())
			subSettings();

		return SwitchSetting.super.addHotkeySetting(whatActivates, defaultTriggerMode);
	}

	@Override
	public ExtendedStorage<Boolean> getStorage() {
		return storage;
	}

}
