/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.settings.types;

import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.settings.AbstractSetting;

import static dev.l3g7.griefer_utils.settings.Settings.settings;
import static dev.l3g7.griefer_utils.settings.types.SwitchSetting.TriggerMode.TOGGLE;

public interface SwitchSetting extends AbstractSetting<SwitchSetting, Boolean> {

	static SwitchSetting create() {return settings.createSwitchSetting();}

	SwitchSetting asCheckbox(); // NOTE: use

	default SwitchSetting addHotkeySetting() {
		return addHotkeySetting(null, TOGGLE);
	}

	SwitchSetting addHotkeySetting(String whatActivates, TriggerMode defaultTriggerMode);

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
