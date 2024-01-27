/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget;

import static net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State.CHECKED;
import static net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State.UNCHECKED;

public class SwitchSettingImpl extends AbstractSettingImpl<SwitchSetting, Boolean> implements SwitchSetting {

	private boolean checkbox = false;

	public SwitchSettingImpl() {
		super(JsonPrimitive::new, JsonElement::getAsBoolean, false);
	}

	@Override
	protected Widget[] createWidgets() {
		if (checkbox) {
			CheckBoxWidget widget = new CheckBoxWidget();
			widget.setState(get() ? CHECKED : UNCHECKED);
			callback(v -> widget.setState(v ? CHECKED : UNCHECKED));

			widget.setPressable(() -> set(widget.state() == CHECKED));

			return new Widget[]{widget};
		} else {
			SwitchWidget widget = SwitchWidget.text("An", "Aus", this::set);
			widget.setValue(get());
			callback(widget::setValue);

			return new Widget[]{widget};
		}
	}

	@Override
	public SwitchSetting asCheckbox() {
		checkbox = true;
		return this;
	}

}