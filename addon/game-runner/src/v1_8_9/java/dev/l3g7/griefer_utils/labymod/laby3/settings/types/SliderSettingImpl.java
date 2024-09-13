/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.core.settings.types.SliderSetting;
import net.labymod.settings.elements.SliderElement;

public class SliderSettingImpl extends SliderElement implements Laby3Setting<SliderSetting, Integer>, SliderSetting {

	private final ExtendedStorage<Integer> storage = new ExtendedStorage<>(JsonPrimitive::new, JsonElement::getAsInt, 0);

	public SliderSettingImpl() {
		super("§cNo name set", null, 0);
		addCallback(this::set);
	}

	@Override
	public ExtendedStorage<Integer> getStorage() {
		return storage;
	}

	@Override
	public SliderSetting min(int min) {
		return (SliderSetting) setMinValue(min);
	}

	@Override
	public SliderSetting max(int max) {
		return (SliderSetting) setMaxValue(max);
	}

}
