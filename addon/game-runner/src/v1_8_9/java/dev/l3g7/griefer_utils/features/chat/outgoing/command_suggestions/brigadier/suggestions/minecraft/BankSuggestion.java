package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.minecraft;

import dev.l3g7.griefer_utils.core.misc.griefer_games.ScoreboardSource;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.Suggestion;

/**
 * A suggestion for the current money deposited on GrieferGames.
 */
public class BankSuggestion extends Suggestion {

	@Override
	public boolean canUse() {
		return ScoreboardSource.getBankBalance().isSet();
	}

	@Override
	public String get() {
		long balance = ScoreboardSource.getBankBalance().getOr(0L);
		return Long.toString(balance);
	}

}
