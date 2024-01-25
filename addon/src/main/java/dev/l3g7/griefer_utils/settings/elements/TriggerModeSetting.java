/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.misc.Named;

import java.util.function.Consumer;

import static dev.l3g7.griefer_utils.settings.elements.TriggerModeSetting.TriggerMode.TOGGLE;

public class TriggerModeSetting extends DropDownSetting<TriggerModeSetting.TriggerMode> {

	public TriggerModeSetting() {
		super(TriggerMode.class);
		name("Auslösung");
		icon("lightning");
		defaultValue(TOGGLE);
	}

	@Override
	public TriggerModeSetting callback(Consumer<TriggerMode> callback) {
		return (TriggerModeSetting) super.callback(callback);
	}

	@Override
	public TriggerModeSetting description(String... description) {
		return (TriggerModeSetting) super.description(description);
	}

	@Override
	public TriggerModeSetting callback(Runnable callback) {
		return (TriggerModeSetting) super.callback(callback);
	}


	@Override
	public TriggerModeSetting defaultValue(TriggerMode value) {
		return ((TriggerModeSetting) super.defaultValue(value));
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
