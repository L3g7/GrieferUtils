/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.botd.handler;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.ScoreboardUpdateEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.features.widgets.botd.Reward;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.features.widgets.botd.handler.BlockOfTheDayHandler.currentServerDay;

class RewardHandler {

	private static final Pattern REWARD_COUNT_PATTERN = Pattern.compile(".*/(\\d+)");
	private static long lastReportedInfer = 0;
	private static Reward reward = null;
	private static long rewardReceived = 0;

	public static int counter = 1;

	public static boolean shouldSend() {
		return counter != -1;
	}

	public static int getCounter(Reward.RewardType type) {
		if (type != Reward.RewardType.CUSTOM && !BlockOfTheDayHandler.isEvent) {
			counter = -1;
			return type.defaultAmount;
		}

		return counter++;
	}

	@EventListener
	private static void onGGJoin(GrieferGamesJoinEvent e) {
		counter = 1;
	}

	public static void send(Reward reward) {
		if (RewardHandler.shouldSend())
			GUServer.sendBlockOfTheDayReward(reward.type.toString().toLowerCase(), RewardHandler.getCounter(reward.type), reward.amount, reward.eventItem);
	}

	@EventListener
	private static void onMessageReceive(MessageEvent.MessageReceiveEvent event) {
		String msg = event.message.getFormattedText();
		if (msg.equals("§r§8[§r§6Block des Tages§r§8] §r§aDu hast ein seltenes Sammel-Item erhalten!§r")) {
			Reward reward = SpecialRewardHandler.parseReward();
			if (reward != null)
				send(reward);
			return;
		}

		if (msg.equals("§r§8[§r§6Block des Tages§r§8] §r§aDu hast eine Belohnung erhalten.§r")) {
			if (reward != null && rewardReceived >= System.currentTimeMillis() - 100)
				send(reward);
			return;
		}

		Reward newReward = Reward.RewardType.MONEY.getReward(msg);
		if (newReward == null)
			newReward = Reward.RewardType.CRYSTALS.getReward(msg);

		if (newReward != null) {
			rewardReceived = System.currentTimeMillis();
			reward = newReward;
		}
	}

	@EventListener
	private static void onScoreboardUpdate(ScoreboardUpdateEvent event) {
		if (!event.key.equals("Block des Tages"))
			return;

		Matcher matcher = REWARD_COUNT_PATTERN.matcher(event.value);
		if (!matcher.matches())
			return;

		if (lastReportedInfer == currentServerDay())
			return;

		lastReportedInfer = currentServerDay();
		GUServer.sendBlockOfTheDayInferReward(Integer.parseInt(matcher.group(1)));
	}
}
