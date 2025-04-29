/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized;

import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

/**
 * Gathers the current item displayed by the "Block des Tages"-GUI and reports it to the server.
 */
public class BlockOfTheDay {

	private static long lastReportedBlock = 0;
	private static Reward reward = null;
	private static long rewardReceived = 0;
	private static boolean rewardSent = false;

	@EventListener
	public static void onTick(ClientTickEvent event) {
		if (!(mc().currentScreen instanceof GuiChest))
			return;

		IInventory inventory = Reflection.get(mc().currentScreen, "lowerChestInventory");
		if (!inventory.getDisplayName().getFormattedText().equals("§6Block des Tages§r"))
			return;

		if (inventory.getSizeInventory() != 27 || inventory.getStackInSlot(13) == null)
			return;

		long now = System.currentTimeMillis() / 1000;
		long currentDay = now / 86400 * 86400 + 7200; // Updates happen at 02:00 GMT+0 every night
		if (currentDay > now)
			currentDay -= 86400;

		if (lastReportedBlock == currentDay)
			return;

		lastReportedBlock = currentDay;
		ItemStack stack = inventory.getStackInSlot(13);
		String lastLore = ItemUtil.getLastLore(stack).replaceAll("§.", "");
		String bdtEvent = null;

		if (lastLore.toLowerCase().startsWith("event:"))
			bdtEvent = lastLore.substring("event:".length()).trim();

		ResourceLocation resLoc = Item.itemRegistry.getNameForObject(stack.getItem());
		String id = resLoc == null ? "air" : resLoc.getResourcePath();
		int damage = stack.getItemDamage();
		GUServer.sendBlockOfTheDayBlock(id, damage, bdtEvent);
	}

	@EventListener
	private static void onMessageReceive(MessageReceiveEvent event) {
		String msg = event.message.getFormattedText();
		if (msg.equals("§r§8[§r§6Block des Tages§r§8] §r§aDu hast ein seltenes Sammel-Item erhalten!§r")) {
			new Reward(Reward.RewardType.BLOCK, 2).send();
			return;
		}

		if (msg.equals("§r§8[§r§6Block des Tages§r§8] §r§aDu hast eine Belohnung erhalten.§r")) {
			if (reward == null || rewardReceived < System.currentTimeMillis() - 1000)
				return;

			reward.send();
			return;
		}

		for (Reward.RewardType type : Reward.RewardType.values()) {
			if ((reward = type.getReward(msg)) == null)
				continue;

			rewardReceived = System.currentTimeMillis();
			return;
		}
	}

	private static class Reward {

		private final RewardType type;
		private final int amount;

		private Reward(RewardType type, int amount) {
			this.type = type;
			this.amount = amount;
		}

		public void send() {
			if (rewardSent)
				return;

			rewardSent = true;
			GUServer.sendBlockOfTheDayReward(type.toString().toLowerCase(), amount);
		}

		private enum RewardType {

			MONEY("§r§a\\$((?:\\d+[.,]?)+) wurden zu deinem Konto hinzugefügt\\.§r"),
			CRYSTALS("§r§8\\[§r§bCase§r§fOpening§r§8] §r§aDir wurden §r§2(\\d+) Kristalle §r§agutgeschrieben!§r"),
			BLOCK(null);

			private final Pattern pattern;

			RewardType(String pattern) {
				this.pattern = pattern == null ? null : Pattern.compile(pattern);
			}

			public Reward getReward(String formattedMessage) {
				if (this == BLOCK)
					return null;

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

}