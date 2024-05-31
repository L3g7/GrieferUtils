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

public interface DropDownSetting<E extends Enum<E> & Named> extends AbstractSetting<DropDownSetting<E>, E> {

	static <E extends Enum<E> & Named> DropDownSetting<E> create(Class<E> enumClass) {return settings.createDropDownSetting(enumClass);}

}
