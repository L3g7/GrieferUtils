/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.player.scoreboard;


import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;

@Singleton
public class ScoreboardHandler {

	private static final List<ScoreboardMod> info = new ArrayList<>();

	@SuppressWarnings("unused") // Invoked via asm
	public static boolean shouldUnlockScoreboard() {
		return ServerCheck.isOnGrieferGames() && info.stream().anyMatch(Feature::isEnabled);
	}

	Scoreboard sb;
	ScoreObjective so;
	private final List<Score> hiddenScores = new ArrayList<>();

	@EventListener
	public void onTick(TickEvent.ClientTickEvent tickEvent) {
		if (!ServerCheck.isOnGrieferGames() || world() == null)
			return;

		sb = world().getScoreboard();
		so = sb.getObjectiveInDisplaySlot(1);

		// Check if scoreboard is loading
		Optional<Score> playTimeScore = sb.getSortedScores(so).stream().filter(s -> s.getScorePoints() == 3).findFirst();
		if (!playTimeScore.isPresent())
			return;

		Optional<ScorePlayerTeam> playTimeTeam = sb.getTeams().stream().filter(t -> t.getMembershipCollection().contains(playTimeScore.get().getPlayerName())).findFirst();
		if (!playTimeTeam.isPresent() || (playTimeTeam.get().getColorPrefix().equals("§f§oLaden")))
			return;

		// Get the highest expected score
		List<ScoreboardMod> activeInfoProvider = info.stream().filter(Feature::isEnabled).collect(Collectors.toList());
		int highestExpectedScore = 14 + activeInfoProvider.size() * 3;

		// Get the highest existing score
		OptionalInt optionalHighestScore = sb.getSortedScores(so).stream().mapToInt(Score::getScorePoints).max();
		if (!optionalHighestScore.isPresent())
			return;

		int highestScore = optionalHighestScore.getAsInt();

		if (highestExpectedScore != highestScore) {
			removeCustomScores(highestScore);

			if (highestExpectedScore > highestScore)
				addCustomScores(activeInfoProvider);
		}

		// Update the values
		for (ScoreboardMod mod : activeInfoProvider)
			sb.getTeam(mod.team).setNameSuffix(mod.getValue());

		if (activeInfoProvider.isEmpty()) {
			for (Score score : hiddenScores)
				sb.getValueFromObjective(score.getPlayerName(), so).setScorePoints(score.getScorePoints());
		} else {
			sb.getSortedScores(so).stream().filter(x -> x.getScorePoints() < 3).forEach(s -> {
				hiddenScores.add(s);
				sb.removeObjectiveFromEntity(s.getPlayerName(), s.getObjective());
			});
		}
	}

	private void removeCustomScores(int highestScore) {

		Map<String, Map<ScoreObjective, Score>> objectives = Reflection.get(sb, "entitiesScoreObjectives");

		AtomicInteger removedScores = new AtomicInteger();

		objectives.values().removeIf(map -> {
			Score score = map.get(so);
			if (score == null)
				return false;

			int points = score.getScorePoints();
			if (points > 7 && points < highestScore - 6) {
				removedScores.getAndIncrement();
				return true;
			}

			return false;
		});


		moveScores(-removedScores.get());
	}

	private void addCustomScores(List<ScoreboardMod> activeInfoProvider) {
		// Make place for the new score ones
		moveScores(activeInfoProvider.size() * 3);

		for (int i = 0; i < activeInfoProvider.size(); i++) {
			ScoreboardMod mod = activeInfoProvider.get(i);

			int score = i * 3 + 8;

			updateScore("§7ᐅ §3§l" + mod.name, score + 2); // Title
			updateScore("§f", score + 1); // Value
			updateScore("§f", score); // Space

			// Initialize team
			if (sb.getTeam(mod.team) == null)
				sb.createTeam(mod.team);

			String player = String.format("§0%s§f", getPrefixFromScore(score + 1));
			sb.addPlayerToTeam(player, mod.team);
		}
	}

	private String getPrefixFromScore(int score) {
		String prefix = String.valueOf(score)
			.replace("", "§"); // Add § in front of each digit
		return prefix.substring(0, prefix.length() - 1); // Remove trailing §
	}

	private void updateScore(String text, int score) {
		sb.getValueFromObjective("§0" + getPrefixFromScore(score) + text, so).setScorePoints(score);
	}

	private void moveScores(int delta) {
		sb.getSortedScores(so).stream()
			.filter(e -> e.getScorePoints() > 7)
			.forEach(e -> e.setScorePoints(e.getScorePoints() + delta));
	}

	abstract static class ScoreboardMod extends Feature {

		private final String name;
		private final String team;
		private final int sortingId;

		public ScoreboardMod(String name, int sortingId) {
			this.name = name;
			this.team = new Random().ints(16).mapToObj(i -> Character.toString((char) i)).collect(Collectors.joining());
			this.sortingId = sortingId;

			info.add(this);
			info.sort(Comparator.comparingInt(s -> s.sortingId));
		}

		protected abstract String getValue();

	}

}