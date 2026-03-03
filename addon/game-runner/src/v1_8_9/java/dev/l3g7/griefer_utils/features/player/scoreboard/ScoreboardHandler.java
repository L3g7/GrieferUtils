/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.scoreboard;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import net.labymod.api.client.component.format.numbers.NumberFormat;
import net.labymod.api.client.scoreboard.ScoreboardObjective;
import net.labymod.api.client.scoreboard.ScoreboardScore;
import net.labymod.core.client.gui.hud.hudwidget.ScoreboardHudWidget;
import net.labymod.ingamegui.modules.ScoreboardModule;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

public class ScoreboardHandler {

	public static final LineProvider[] providers = FileProvider.getClassesWithInterface(LineProvider.class)
		.stream()
		.map(ClassMeta::load)
		.map(FileProvider::getSingleton)
		.map(t -> (LineProvider) t)
		.toArray(LineProvider[]::new);

	private static final String[] keys = new String[]{
		"server",
		"money",
		"bank", // new
		"orb", // new
		"online",
		"playtime",
		"address",
	};

	@Mixin(NetHandlerPlayClient.class)
	private static class MixinNetHandlerPlayClient {

		@Inject(method = "handleScoreboardObjective", at = @At("TAIL"))
		public void onHandleScoreboardObjective(S3BPacketScoreboardObjective packet, CallbackInfo ci) {
			if (packet.func_149338_e() != 0 || !packet.func_149337_d().equals("§6§lGrieferGames"))
				return;

			// Build scoreboard
			for (LineProvider provider : providers)
				provider.createLine();

			update();
		}

		/**
		 * Hides lines and overrides scores.
		 */
		@Inject(method = "handleUpdateScore", at = @At("TAIL"))
		public void onHandleUpdateScore(S3CPacketUpdateScore packet, CallbackInfo ci) {
			if (packet.getScoreAction() != S3CPacketUpdateScore.Action.CHANGE)
				return;

			update();
		}
	}

	public static void update() {
		if (world() == null || !ServerCheck.isOnGrieferGames())
			return;

		Scoreboard scoreboard = world().getScoreboard();
		for (Score score : scoreboard.getSortedScores(getGGObjective())) {
			ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
			score.setScorePoints(getScore(team));
		}
	}

	public static int getScore(ScorePlayerTeam team) {
		String key = team.getRegisteredName().split("_")[0];
		if (key.equals("color")) {
			int idx = Integer.parseInt(team.getRegisteredName().split("_")[1]);
			int score = 2;
			for (int i = keys.length - 1; i >= 0; i--) {
				if (shouldHide(keys[i]))
					continue;

				if (--idx == 0)
					return score;

				score += 3;
			}

			return -99;
		}

		int score = getKeyScore(key);
		if (team.getRegisteredName().endsWith("_title"))
			score += 1;

		return score;
	}

	private static int getKeyScore(String key) {
		if (shouldHide(key))
			return -99;

		int score = 0;
		for (int i = keys.length - 1; i >= 0; i--) {
			if (keys[i].equals(key))
				return score;

			if (!shouldHide(keys[i]))
				score += 3;
		}

		throw new IllegalStateException("Unrecognized key " + key);
	}

	private static boolean shouldHide(String key) {
		for (LineProvider provider : providers)
			if (provider.shouldHide(key))
				return true;

		return false;
	}

	private static ScoreObjective getGGObjective() {
		if (world() == null)
			return null;

		Scoreboard scoreboard = world().getScoreboard();
		for (ScoreObjective scoreObjective : scoreboard.getScoreObjectives())
			if (scoreObjective.getDisplayName().equals("§6§lGrieferGames"))
				return scoreObjective;

		return null;
	}

	public static int getScoreboardSize() {
		if (world() == null || !ServerCheck.isOnGrieferGames())
			return 15;

		int size = 0;
		for (Score score : world().getScoreboard().getScores())
			if (score.getScorePoints() >= 0)
				size++;

		return size;
	}

	public interface LineProvider {
		void createLine();

		boolean shouldHide(String key);

		default void updateTeam(String key, String prefix, String suffix) {
			if (getGGObjective() == null)
				return;

			Scoreboard scoreboard = world().getScoreboard();
			ScorePlayerTeam team = scoreboard.getTeam(key);
			if (team == null) {
				team = scoreboard.createTeam(key);
				var name = UUID.randomUUID().toString().replaceAll("-", "").replaceAll("(.)", "§$1").substring(0, 16);
				var score = scoreboard.getValueFromObjective(name, getGGObjective());
				score.setScorePoints(-99);
				scoreboard.addPlayerToTeam(name, key);
			}

			team.setNamePrefix(prefix);
			team.setNameSuffix(suffix);
		}
	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = ScoreboardModule.class, remap = false)
	private static class MixinScoreboardModule {

		@ModifyConstant(method = "renderScoreboard", constant = @Constant(intValue = 15), remap = false)
		private int modifyMaxScoreboardSize(int listSize) {
			return getScoreboardSize();
		}

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = ScoreboardHudWidget.class, remap = false)
	private static class MixinScoreboardModule0 {

		@ModifyConstant(method = "getVisibleScores", constant = @Constant(longValue = 15), remap = false)
		private long modifyMaxScoreboardSize(long limit) {
			return getScoreboardSize();
		}

		@Inject(method = "getVisibleScores", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"), locals = LocalCapture.CAPTURE_FAILHARD)
		private void fixSlicing(net.labymod.api.client.scoreboard.Scoreboard scoreboard, ScoreboardObjective objective, NumberFormat numberFormat, CallbackInfoReturnable<List<?>> cir, Collection<?> scores, List<ScoreboardScore> toSort, List<?> list, long limit) {
			if (world() == null || !ServerCheck.isOnGrieferGames())
				return;

			int maxSize = getScoreboardSize();
			if (toSort.size() > maxSize) {
				ArrayList<ScoreboardScore> cpy = Lists.newArrayList(Iterables.skip(toSort, toSort.size() - maxSize));
				toSort.clear();
				toSort.addAll(cpy);
			}
		}

	}

	@Mixin(GuiIngame.class)
	private static class MixinGuiIngame {

		@ModifyConstant(method = "renderScoreboard", constant = @Constant(intValue = 15))
		private int modifyMaxScoreboardSize(int listSize) {
			return getScoreboardSize();
		}

	}

}