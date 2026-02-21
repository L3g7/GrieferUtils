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
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.utils.Consumer;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.settings.types.SwitchSetting.TriggerMode.HOLD;
import static dev.l3g7.griefer_utils.core.settings.types.SwitchSetting.TriggerMode.TOGGLE;

public class SwitchSettingImpl extends BooleanElement implements Laby3Setting<SwitchSetting, Boolean>, SwitchSetting {

	private final ExtendedStorage<Boolean> storage = new ExtendedStorage<>(JsonPrimitive::new, JsonElement::getAsBoolean, false);
	private TriggerMode previousMode; // NOTE: refactor

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
	public SwitchSetting addHotkeySetting(String whatActivates, TriggerMode defaultTriggerMode) {
		if (getSubSettings().getElements().isEmpty())
			subSettings();

		DropDownSetting<TriggerMode> triggerMode = DropDownSetting.create(TriggerMode.class)
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
				SwitchSettingImpl.this.enabled(m == TOGGLE);
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

		addSetting(4, key);
		addSetting(5, notify);

		// TODO cleanup
		if (defaultTriggerMode == null)
			return this;

		addSetting(6, triggerMode);
		addSetting(7, HeaderSetting.create());
		return this;
	}

	@Override
	public ExtendedStorage<Boolean> getStorage() {
		return storage;
	}

}
