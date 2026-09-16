/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.internal;

import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.Suggestion;

/**
 * A fixed text suggestion.
 */
public class NativeLiteralSuggestion extends Suggestion {
	private final String literal;

	public NativeLiteralSuggestion(String literal) {
		this.literal = literal;
	}

	@Override
	public String get() {
		return literal;
	}
}
