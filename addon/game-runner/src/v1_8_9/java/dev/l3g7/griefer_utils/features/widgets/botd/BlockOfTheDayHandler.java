/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.botd;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * Gathers the current item displayed by the "Block des Tages"-GUI and reports it to the server.
 */
public class BlockOfTheDayHandler {

	private static long lastReportedBlock = 0;
	private static Reward reward = null;
	private static long rewardReceived = 0;
	static boolean isEvent = true;

	@EventListener
	private static void onEquipment(PacketReceiveEvent<S04PacketEntityEquipment> event) {
		if (event.packet.getEquipmentSlot() != 4)
			return;

		ItemStack stack = event.packet.getItemStack();
		if (stack == null)
			return;

		List<String> lore = ItemUtil.getLore(stack);
		if (lore.size() < 3)
			return;

		String lastLore = lore.get(lore.size() - 1).replaceAll("§.", "").toLowerCase();
		String secondLastLore = lore.get(lore.size() - 2).replaceAll("§.", "").toLowerCase();

		if (!(secondLastLore.equals("baue diesen block ab, um mit etwas") && lastLore.equals("glück einen gewinn zu bekommen."))
			&& !lastLore.startsWith("event:"))
			return;

		// It's a block of the day item :D

		long now = System.currentTimeMillis() / 1000;
		long currentDay = now / 86400 * 86400 + 7200; // Updates happen at 02:00 GMT+0 every night
		if (currentDay > now)
			currentDay -= 86400;

		if (lastReportedBlock == currentDay)
			return;

		if (stack.getDisplayName().replaceAll("§.", "").toLowerCase().startsWith("fehler"))
			return;

		lastReportedBlock = currentDay;

		String bdtEvent = null;

		if (lastLore.toLowerCase().startsWith("event:"))
			bdtEvent = lastLore.substring("event:".length()).trim();

		ResourceLocation resLoc = Item.itemRegistry.getNameForObject(stack.getItem());
		String id = resLoc == null ? "air" : resLoc.getResourcePath();
		int damage = stack.getItemDamage();
		isEvent = bdtEvent != null;
		GUServer.sendBlockOfTheDayBlock(id, damage, bdtEvent);
	}

	@EventListener
	private static void onMessageReceive(MessageReceiveEvent event) {
		String msg = event.message.getFormattedText();
		if (msg.equals("§r§8[§r§6Block des Tages§r§8] §r§aDu hast ein seltenes Sammel-Item erhalten!§r")) {
			Reward reward = SpecialRewardHandler.parseReward();
			if (reward != null)
				reward.send();
			return;
		}

		if (msg.equals("§r§8[§r§6Block des Tages§r§8] §r§aDu hast eine Belohnung erhalten.§r")) {
			if (reward != null && rewardReceived >= System.currentTimeMillis() - 100)
				reward.send();
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

}