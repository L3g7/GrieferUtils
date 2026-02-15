/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.scoreboard;


import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.WorldUnloadEvent;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.core.client.gui.hud.hudwidget.ScoreboardHudWidget;
import net.labymod.ingamegui.modules.ScoreboardModule;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

@Singleton
public class ScoreboardHandler {

	private static final List<ScoreboardMod> info = new ArrayList<>();
	private static final CleanupScoreboard cleanUpscoreboard = FileProvider.getSingleton(CleanupScoreboard.class);

	private final HiddenScore[] allHiddenScores = new HiddenScore[] {
		// ip
		new HiddenScore(0, 3, () -> info.stream().anyMatch(Feature::isEnabled) || (cleanUpscoreboard.isEnabled() && cleanUpscoreboard.ip.get())),
		// play time
		new HiddenScore(3, 6, () -> cleanUpscoreboard.isEnabled() && cleanUpscoreboard.playTime.get())
	};

	public static boolean shouldUnlockScoreboard() {
		return ServerCheck.isOnGrieferGames() && info.stream().anyMatch(Feature::isEnabled);
	}

	Scoreboard sb;
	ScoreObjective so;

	@EventListener
	private void onTick(TickEvent.ClientTickEvent tickEvent) {
		if (!ServerCheck.isOnGrieferGames() || world() == null)
			return;

		sb = world().getScoreboard();
		so = sb.getObjectiveInDisplaySlot(1);

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

		for (HiddenScore hiddenScore : allHiddenScores)
			hiddenScore.update();
	}

	@EventListener
	private void onWorldLeave(WorldUnloadEvent event) {
		for (HiddenScore hiddenScore : allHiddenScores)
			hiddenScore.scores.clear();
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

	private class HiddenScore {

		private final int min;
		private final int max;
		private final Supplier<Boolean> isHidden;

		private final List<Score> scores = new ArrayList<>();

		private HiddenScore(int min, int max, Supplier<Boolean> isHidden) {
			this.min = min;
			this.max = max;
			this.isHidden = isHidden;
		}

		public void update() {
			if (isHidden.get()) {
				// Hide
				sb.getSortedScores(so).stream().filter(x -> x.getScorePoints() >= min && x.getScorePoints() < max).forEach(s -> {
					this.scores.add(s);
					sb.removeObjectiveFromEntity(s.getPlayerName(), s.getObjective());
				});
			} else {
				// Show
				for (Score score : this.scores)
					sb.getValueFromObjective(score.getPlayerName(), so).setScorePoints(score.getScorePoints());
			}
		}

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

	@ExclusiveTo(LABY_3)
	@Mixin(value = ScoreboardModule.class, remap = false)
	private static class MixinScoreboardModule {

		@ModifyConstant(method = "renderScoreboard", constant = @Constant(intValue = 15), remap = false)
		private int modifyMaxScoreboardSize(int listSize) {
			if (shouldUnlockScoreboard())
				return Integer.MAX_VALUE;

			return listSize;
		}

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = ScoreboardHudWidget.class, remap = false)
	private static class MixinScoreboardModule0 {

		@ModifyConstant(method = "getVisibleScores", constant = @Constant(longValue = 15), remap = false)
		private long modifyMaxScoreboardSize(long limit) {
			if (shouldUnlockScoreboard())
				return Integer.MAX_VALUE;

			return limit;
		}

	}

	@Mixin(GuiIngame.class)
	private static class MixinGuiIngame {

		@ModifyConstant(method = "renderScoreboard", constant = @Constant(intValue = 15))
		private int modifyMaxScoreboardSize(int listSize) {
			if (shouldUnlockScoreboard())
				return Integer.MAX_VALUE;

			return listSize;
		}

	}

}