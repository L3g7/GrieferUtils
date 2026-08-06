package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.minecraft;

import dev.l3g7.griefer_utils.core.misc.griefer_games.Citybuild;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.Requirement;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

/**
 * A requirement for the player to be on fixed GrieferGames subserver.
 */
public class SubserverRequirement extends Requirement {

	private final String server;

	public SubserverRequirement(String server) {
		this.server = server;
	}

	@Override
	public boolean test() {
		if (world() == null)
			// Fall back to true
			return true;

		String server = Citybuild.currentRaw().trim();
		if (server.isEmpty())
			// Fall back to true
			return true;

		return this.server.equalsIgnoreCase(server);
	}

}
