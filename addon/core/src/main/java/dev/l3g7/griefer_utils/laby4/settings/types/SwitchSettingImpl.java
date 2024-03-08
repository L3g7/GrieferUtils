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
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.KeySetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget;

import static dev.l3g7.griefer_utils.settings.types.SwitchSetting.TriggerMode.HOLD;
import static net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State.CHECKED;
import static net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State.UNCHECKED;

public class SwitchSettingImpl extends AbstractSettingImpl<SwitchSetting, Boolean> implements SwitchSetting {

	private boolean checkbox = false;
	private TriggerMode previousMode; // NOTE: refactor

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

	@Override
	public SwitchSetting addHotkeySetting(String whatActivates, TriggerMode defaultTriggerMode) {
		DropDownSettingImpl<TriggerMode> triggerMode = (DropDownSettingImpl<TriggerMode>) DropDownSetting.create(TriggerMode.class)
			.name("Auslösung")
			.icon("lightning")
			.inferConfig("triggerMode")
			.defaultValue(defaultTriggerMode)
			.callback(m -> {
				if (previousMode != null && previousMode != m)
					SwitchSettingImpl.this.set(false);

				previousMode = m;
			});

		if (defaultTriggerMode != null) {
			addSetting(0, HeaderSetting.create());
			addSetting(0, triggerMode);
		}

		KeySettingImpl key = (KeySettingImpl) KeySetting.create()
			.name("Taste")
			.icon("key")
			.inferConfig("key")
			.pressCallback(p -> {
				if (p || triggerMode.get() == HOLD)
					this.set(!this.get());
			});
		addSetting(0, key);

		key.description("Welche Taste " + whatActivates + " aktiviert.");
		triggerMode.description("Halten: Aktiviert " + whatActivates + ", während die Taste gedrückt wird.",
			"Umschalten: Schaltet " + whatActivates + " um, wenn die Taste gedrückt wird.");
		return this;
	}

}
