package dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.requirements.minecraft;

import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.requirements.Requirement;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

/**
 * A requirement for the player to have a fixed GrieferGames rank.
 */
public class RankRequirement extends Requirement {

	private final String rank;

	public RankRequirement(String rank) {
		this.rank = rank;
	}

	@Override
	public boolean test() {
		if (player() == null)
			// Fall back to true
			return true;

		String rank = player().getDisplayName().getUnformattedText().split("┃")[0].trim();
		return this.rank.equalsIgnoreCase(rank);
	}

}
