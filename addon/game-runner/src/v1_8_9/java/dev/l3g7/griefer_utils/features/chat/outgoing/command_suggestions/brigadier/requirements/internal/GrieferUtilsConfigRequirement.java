/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.Requirement;

/**
 * A requirement for a true boolean value in GrieferUtils' config.
 */
public class GrieferUtilsConfigRequirement extends Requirement {

	private final String key;

	@SerializedName("default")
	private final boolean defaultValue;

	public GrieferUtilsConfigRequirement(String key, boolean defaultValue) {
		this.key = key;
		this.defaultValue = defaultValue;
	}

	@Override
	public boolean test() {
		return Config.has(key) ? Config.get(key).getAsJsonPrimitive().getAsBoolean() : defaultValue;
	}

}
