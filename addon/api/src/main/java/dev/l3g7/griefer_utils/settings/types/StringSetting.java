/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.settings.types;

import dev.l3g7.griefer_utils.api.misc.functions.Predicate;
import dev.l3g7.griefer_utils.settings.AbstractSetting;

import static dev.l3g7.griefer_utils.settings.Settings.settings;

public interface StringSetting extends AbstractSetting<StringSetting, String> {

	static StringSetting create() {return settings.createStringSetting();}

	StringSetting maxLength(int maxLength);

	StringSetting placeholder(String placeholder);

	/**
	 * Sets a validation predicate for the input.
	 * If an input fails the validation, the setting gets marked and doesn't update its storage.
	 */
	StringSetting validator(Predicate<String> validator);

}
