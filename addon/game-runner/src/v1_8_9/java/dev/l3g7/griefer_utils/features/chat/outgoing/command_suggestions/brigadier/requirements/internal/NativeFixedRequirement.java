/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal;

import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.Requirement;

/**
 * A requirement that is always true / false.
 */
public class NativeFixedRequirement extends Requirement {

	public static final Requirement ALWAYS = new NativeFixedRequirement(true);
	public static final Requirement NEVER = new NativeFixedRequirement(false);

	private final boolean value;

	public NativeFixedRequirement(boolean value) {
		this.value = value;
	}

	@Override
	public boolean test() {
		return value;
	}

}
