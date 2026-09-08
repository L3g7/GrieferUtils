/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.types;

import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.primitives.containers.Option;
import dev.l3g7.griefer_utils.core.settings.AbstractSetting;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.settings.Settings.settings;
import static dev.l3g7.griefer_utils.core.settings.types.SwitchSetting.TriggerMode.HOLD;
import static dev.l3g7.griefer_utils.core.settings.types.SwitchSetting.TriggerMode.TOGGLE;

public interface SwitchSetting extends AbstractSetting<SwitchSetting, Boolean> {

	static SwitchSetting create() {return settings.createSwitchSetting();}

	int getHotkeySettingOffset();

	default SwitchSetting addHotkeySetting(String whatActivates, TriggerMode defaultTriggerMode) {
		Option<TriggerMode> previousMode = Option.emptyMut();

		DropDownSetting<TriggerMode> triggerMode = DropDownSetting.create(TriggerMode.class)
			.name("Auslösung")
			.description("Halten: Aktiviert " + whatActivates + ", während die Taste gedrückt wird.",
				"Umschalten: Schaltet " + whatActivates + " um, wenn die Taste gedrückt wird.")
			.icon("lightning")
			.inferConfig("triggerMode")
			.defaultValue(defaultTriggerMode)
			.callback(m -> {
				if (previousMode.isSet() && previousMode.get() != m)
					set(false);

				previousMode.set(m);
				enabled(m == TOGGLE);
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
					set(!get());
					if (notify.get())
						labyBridge.notify(name(), name() + " wurde " + (get() ? "§aaktiviert" : "§cdeakiviert" + "§r!"));
				}
			});

		int offset = getHotkeySettingOffset();
		addSetting(offset, key, notify);

		if (defaultTriggerMode != null)
			addSetting(offset + 2, triggerMode, HeaderSetting.create());

		return this;
	}

	enum TriggerMode implements Named {

		HOLD("Halten"), TOGGLE("Umschalten");

		final String name;

		TriggerMode(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}


	}

}
