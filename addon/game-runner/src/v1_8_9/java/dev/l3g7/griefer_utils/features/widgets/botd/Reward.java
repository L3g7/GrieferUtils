/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.botd;

import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.events.griefergames.BlockOfTheDayRewardEvent;
import net.minecraft.item.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Reward {

	public final RewardType type;
	private final int amount;
	private final ItemStack eventItem;

	public Reward(RewardType type, int amount) {
		this.type = type;
		this.amount = amount;
		this.eventItem = null;
	}

	public Reward(ItemStack eventItem) {
		type = RewardType.CUSTOM;
		amount = eventItem.stackSize;
		this.eventItem = eventItem;
	}

	public void send() {
		if (RewardCounter.shouldSend()) {
			GUServer.sendBlockOfTheDayReward(type.toString().toLowerCase(), RewardCounter.getCounter(type), amount, eventItem);
			new BlockOfTheDayRewardEvent().fire();
		}
	}

	public enum RewardType {

		MONEY("§r§a\\$((?:\\d+[.,]?)+) wurden zu deinem Konto hinzugefügt\\.§r", 5),
		CRYSTALS("§r§8\\[§r§bCase§r§fOpening§r§8] §r§aDir wurden §r§2(\\d+) Kristalle §r§agutgeschrieben!§r", 3),
		BLOCK(null, 2),
		CUSTOM(null, -1);

		private final Pattern pattern;
		public final int defaultAmount;

		RewardType(String pattern, int defaultAmount) {
			this.pattern = pattern == null ? null : Pattern.compile(pattern);
			this.defaultAmount = defaultAmount;
		}

		public Reward getReward(String formattedMessage) {
			Matcher matcher = pattern.matcher(formattedMessage);
			if (!matcher.matches())
				return null;

			try {
				return new Reward(this, Integer.parseInt(matcher.group(1).replace(",", "").replace(".", "")));
			} catch (NumberFormatException e) {
				BugReporter.reportError(new Throwable("Error parsing BDT from " + formattedMessage));
				return null;
			}
		}

	}

}
