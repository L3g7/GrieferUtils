/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.botd.handler;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

public class BlockOfTheDayHandler {

	private static long lastReportedBlock = 0;
	static boolean isEvent = true;

	static long currentServerDay() {
		long now = System.currentTimeMillis() / 1000;
		long currentDay = now / 86400 * 86400 + 7500; // Updates happen at 04:05 GMT+2 every night
		if (currentDay > now)
			currentDay -= 86400;

		return currentDay;
	}

	@EventListener
	private static void onEquipment(PacketReceiveEvent<S1CPacketEntityMetadata> event) {
		Entity entity = world().getEntityByID(event.packet.getEntityId());
		if (!(entity instanceof EntityArmorStand eas) || entity.getRotationYawHead() == 0)
			return;

		ItemStack stack = eas.getInventory()[4];
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

		if (lastReportedBlock == currentServerDay())
			return;

		if (stack.getDisplayName().replaceAll("§.", "").toLowerCase().startsWith("fehler"))
			return;

		lastReportedBlock = currentServerDay();

		String bdtEvent = null;

		if (lastLore.toLowerCase().startsWith("event:"))
			bdtEvent = lastLore.substring("event:".length()).trim();

		ResourceLocation resLoc = Item.itemRegistry.getNameForObject(stack.getItem());
		String id = resLoc == null ? "air" : resLoc.getResourcePath();
		int damage = stack.getItemDamage();
		isEvent = bdtEvent != null;
		GUServer.sendBlockOfTheDayBlock(id, damage, bdtEvent);
	}

}