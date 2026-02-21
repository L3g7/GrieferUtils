/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.AbstractSettingImpl;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.settings.types.SwitchSetting.TriggerMode.HOLD;

public class SwitchSettingImpl extends AbstractSettingImpl<SwitchSetting, Boolean> implements SwitchSetting {

	private TriggerMode previousMode; // NOTE: refactor

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
	public SwitchSetting addHotkeySetting(String whatActivates, TriggerMode defaultTriggerMode) {
		DropDownSettingImpl<TriggerMode> triggerMode = (DropDownSettingImpl<TriggerMode>) DropDownSetting.create(TriggerMode.class)
			.name("Auslösung")
			.description("Halten: Aktiviert " + whatActivates + ", während die Taste gedrückt wird.",
				"Umschalten: Schaltet " + whatActivates + " um, wenn die Taste gedrückt wird.")
			.icon("lightning")
			.inferConfig("triggerMode")
			.defaultValue(defaultTriggerMode)
			.callback(m -> {
				if (previousMode != null && previousMode != m)
					SwitchSettingImpl.this.set(false);

				previousMode = m;
			});

		SwitchSetting notify = SwitchSetting.create()
			.name("Benachrichtigen")
			.description("Ob ein Popup angezeigt werden soll, wenn " + whatActivates + " de-/aktiviert wird.")
			.icon("bell")
			.inferConfig("notify");

		KeySetting key = KeySetting.create()
			.name("Taste")
			.description("Welche Taste " + whatActivates + " aktiviert.")
			.icon("key")
			.inferConfig("key")
			.pressCallback(p -> {
				if (p || (defaultTriggerMode != null && triggerMode.get() == HOLD)) {
					this.set(!this.get());
					if (notify.get())
						labyBridge.notify(this.name(), this.name() + " wurde " + (this.get() ? "§aaktiviert" : "§cdeakiviert" + "§r!"));
				}
			});

		addSetting(0, notify);

		if (defaultTriggerMode != null) {
			addSetting(0, HeaderSetting.create());
			addSetting(0, triggerMode);
		}

		addSetting(0, key);
		return this;
	}

}
