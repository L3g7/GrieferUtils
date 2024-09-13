/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.core.misc.server.GUClient;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

/**
 * Gathers the current item displayed by the "Block des Tages"-GUI and reports it to the server.
 */
public class BlockOfTheDay {

	private static long lastReportedBlock = 0;

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
		new Thread(() -> GUClient.get().sendBlockOfTheDay(inventory.getStackInSlot(13))).start();
	}

}
