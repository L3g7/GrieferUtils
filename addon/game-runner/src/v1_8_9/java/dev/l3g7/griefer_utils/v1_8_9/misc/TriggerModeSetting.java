/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc;

import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;

import static dev.l3g7.griefer_utils.v1_8_9.misc.TriggerModeSetting.TriggerMode.TOGGLE;

public class TriggerModeSetting {

	public static DropDownSetting<TriggerMode> create() {
		return DropDownSetting.create(TriggerMode.class)
			.name("Auslösung")
			.icon("lightning")
			.defaultValue(TOGGLE);
	}

	public enum TriggerMode implements Named {

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
