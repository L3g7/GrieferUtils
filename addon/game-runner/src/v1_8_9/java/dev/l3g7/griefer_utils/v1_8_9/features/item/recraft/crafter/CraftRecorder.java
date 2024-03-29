/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.crafter;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.WindowClickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.Recraft;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.RecraftAction.Ingredient;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.RecraftRecording;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C01PacketChatMessage;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.RecraftRecording.RecordingMode.CRAFT;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;

public class CraftRecorder {

	private static RecraftRecording recording = Recraft.tempRecording;
	private static GuiScreen previousScreen = null;
	private static boolean addedIcon = false;
	private static boolean isMenuOpen = false;
	private static boolean executedCommand = false;

	public static void startRecording(RecraftRecording recording) {
		if (!ServerCheck.isOnCitybuild()) {
			labyBridge.notify("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild gestartet werden.");
			return;
		}

		CraftRecorder.recording = recording;
		previousScreen = mc().currentScreen;
		addedIcon = false;
		executedCommand = true;
		send("/craft");
	}

	@EventListener
	private static void onMessageSend(PacketSendEvent<C01PacketChatMessage> event) {
		String lowerMsg = event.packet.getMessage().toLowerCase();
		if (!Recraft.isPlaying() && (lowerMsg.equals("/craft") || lowerMsg.startsWith("/craft ")))
			executedCommand = true;
	}

	@EventListener
	private static void onGuiOpen(GuiOpenEvent<?> event) {
		if (Recraft.isPlaying())
			return;

		if (event.gui instanceof GuiCrafting) {
			if (executedCommand) {
				isMenuOpen = true;
				recording.actions.clear();
				recording.mode.set(CRAFT);
				executedCommand = false;
			}
			return;
		}

		recording = Recraft.tempRecording;
		isMenuOpen = false;
		if (previousScreen == null || previousScreen == event.gui)
			return;

		event.cancel();
		mc().displayGuiScreen(previousScreen);
		previousScreen = null;
	}

	@EventListener
	private static void onSendClick(WindowClickEvent event) {
		if (!isMenuOpen)
			return;

		if (event.slotId != 0 || event.itemStack == null)
			return;

		if (!addedIcon) {
			ItemStack targetStack = event.itemStack.copy();
			targetStack.stackSize = ItemUtil.getCompressionLevel(targetStack);
			recording.icon = targetStack;
			addedIcon = true;
		}


		Ingredient[] ingredients = new Ingredient[9];
		for (int i = 0; i < 9; i++)
			ingredients[i] = Ingredient.fromItemStack(player().openContainer.getSlot(i + 1).getStack());

		recording.actions.add(new CraftAction(ingredients));
	}

}