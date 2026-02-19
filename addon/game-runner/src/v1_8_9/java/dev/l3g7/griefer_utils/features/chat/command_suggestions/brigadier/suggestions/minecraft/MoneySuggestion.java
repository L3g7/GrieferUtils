package dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.suggestions.minecraft;

import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.suggestions.Suggestion;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

/**
 * A suggestion for the current money owned on GrieferGames.
 */
public class MoneySuggestion extends Suggestion {

	@Override
	public String get() {
		return world().getScoreboard().getTeam("money_value").getColorPrefix().replaceAll("[$.]", "").replace(",", ".");
	}
}
