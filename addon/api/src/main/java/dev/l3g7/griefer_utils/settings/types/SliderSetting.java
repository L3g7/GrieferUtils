/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.settings.types;

import dev.l3g7.griefer_utils.settings.AbstractSetting;

import static dev.l3g7.griefer_utils.settings.Settings.settings;

public interface SliderSetting extends AbstractSetting<SliderSetting, Integer> {

	static SliderSetting create() {return settings.createSliderSetting();}

	/**
	 * Sets the lower limit the value can have.
	 */
	SliderSetting min(int min);

	/**
	 * Sets the upper limit the value can have.
	 */
	SliderSetting max(int max);

}
