package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.minecraft;

import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.griefer_games.Balances;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.Suggestion;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A suggestion for the current money owned on GrieferGames.
 */
public class MoneySuggestion extends Suggestion {

	@Override
	public boolean canUse() {
		return Balances.getBalance().isSet();
	}

	@Override
	public String get() {
		return Constants.DECIMAL_FORMAT_98.format(
			Balances.getBalance()
				.getOr(BigDecimal.ZERO)
				.setScale(0, RoundingMode.FLOOR))
			.replaceAll("\\.", "");
	}

}
