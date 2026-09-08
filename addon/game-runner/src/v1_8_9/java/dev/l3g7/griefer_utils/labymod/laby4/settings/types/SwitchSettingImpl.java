/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.AbstractSettingImpl;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget;

public class SwitchSettingImpl extends AbstractSettingImpl<SwitchSetting, Boolean> implements SwitchSetting {

	public SwitchSettingImpl() {
		super(JsonPrimitive::new, JsonElement::getAsBoolean, false);
	}

	@Override
	protected Widget[] createWidgets() {
		SwitchWidget widget = SwitchWidget.text("An", "Aus", this::set);
		widget.setValue(get());
		callback(widget::setValue);

		return new Widget[]{widget};
	}

	@Override
	public int getHotkeySettingOffset() {
		return 0;
	}

}
