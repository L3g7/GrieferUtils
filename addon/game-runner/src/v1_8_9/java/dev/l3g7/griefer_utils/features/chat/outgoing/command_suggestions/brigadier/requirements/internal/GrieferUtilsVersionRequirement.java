/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal;

import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.Requirement;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.misc.VersionComparator.VERSION_COMPARATOR;

/**
 * A requirement for GrieferUtils' version to be below a threshold.
 * Negated to preserve backwards compatibility (as unknown requirements default to true)
 */
public class GrieferUtilsVersionRequirement extends Requirement {

	private final String below;

	public GrieferUtilsVersionRequirement(String below) {
		this.below = below;
	}

	@Override
	public boolean test() {
		return VERSION_COMPARATOR.compare(below, labyBridge.addonVersion()) < 0;
	}

}
