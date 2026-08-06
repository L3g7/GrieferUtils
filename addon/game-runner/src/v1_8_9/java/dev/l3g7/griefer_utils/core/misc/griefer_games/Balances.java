package dev.l3g7.griefer_utils.core.misc.griefer_games;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.Option;
import dev.l3g7.griefer_utils.core.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Cash and bank balance.
 */
public class Balances {

	private static long balance = 0;

	private static Option<String> getKV(String key) {
		WorldClient world = MinecraftUtil.world();
		if (world == null)
			return Option.empty();

		Scoreboard scoreboard = world.getScoreboard();
		if (scoreboard == null)
			return Option.empty();

		ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
		if (objective == null)
			return Option.empty();

		ArrayList<Score> scores = new ArrayList<>(scoreboard.getSortedScores(objective));
		ListIterator<Score> scoresIter = scores.listIterator(scores.size());

		String needle = "ᐅ " + key;
		while (scoresIter.hasPrevious()) {
			String title = getScoreText(scoresIter.previous());
			if (title.equals(needle) && scoresIter.hasPrevious())
				return Option.of(getScoreText(scoresIter.previous()));
		}

		return Option.empty();
	}

	private static String getScoreText(Score score) {
		ScorePlayerTeam team = score.getScoreScoreboard().getPlayersTeam(score.getPlayerName());
		if (team == null)
			return score.getPlayerName().replaceAll("§.", "");
		else
			return team.formatString(score.getPlayerName()).replaceAll("§.", "");
	}

	/**
	 * Get the current balance based on the scoreboard value.
	 */
	public static Option<BigDecimal> getBalance() {
		return getKV("Kontostand").map(value ->
			new BigDecimal(
				value.replaceAll("§.", "")
					.replaceAll("[$.]", "")
					.replace(",", ".")));
	}

	/**
	 * Get the current bank balance based on the scoreboard value.
	 */
	public static long getBankBalance() {
		return balance;
	}

	@EventListener
	private static void onMMCustomPayload(MysteryModPayloadEvent event) {
		if (!event.channel.equals("bank"))
			return;

		JsonObject payload = event.payload.getAsJsonObject();
		balance = payload.get("amount").getAsLong();
	}

}