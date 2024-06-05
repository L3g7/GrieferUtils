/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.types;

import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import dev.l3g7.griefer_utils.core.settings.Settings;

import java.util.Set;

public interface KeySetting extends AbstractSetting<KeySetting, Set<Integer>> {

	static KeySetting create() {return Settings.settings.createKeySetting();}

	KeySetting placeholder(String placeholder);

	/**
	 * Registers a callback for button presses.
	 * @param callback true if button is pressed, false if released.
	 */
	KeySetting pressCallback(Consumer<Boolean> callback);

	/**
	 * Enables detection of button presses even if a GUI is open.
	 */
	KeySetting triggersInContainers();

}
