/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.minecraft;

import dev.l3g7.griefer_utils.core.misc.griefer_games.Balances;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.Suggestion;

/**
 * A suggestion for the current money deposited on GrieferGames.
 */
public class BankSuggestion extends Suggestion {

	@Override
	public String get() {
		return Long.toString(Balances.getBankBalance());
	}

}
