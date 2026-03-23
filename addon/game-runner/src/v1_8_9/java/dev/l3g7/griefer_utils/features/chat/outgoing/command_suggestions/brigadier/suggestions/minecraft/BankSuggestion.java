package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.minecraft;

import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.Suggestion;
import dev.l3g7.griefer_utils.features.world.scoreboard.BankScoreboard;

/**
 * A suggestion for the current money deposited on GrieferGames.
 */
public class BankSuggestion extends Suggestion {

	@Override
	public String get() {
		return Long.toString(BankScoreboard.getBankBalance());
	}
}
