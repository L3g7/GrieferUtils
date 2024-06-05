/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.laby4.decompressor;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.features.item.recraft.laby4.Recraft;
import dev.l3g7.griefer_utils.features.item.recraft.laby4.RecraftAction.Ingredient;
import dev.l3g7.griefer_utils.features.item.recraft.laby4.RecraftRecording;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@ExclusiveTo(LABY_4)
public class DecompressRecorder {

	private static RecraftRecording recording = Recraft.tempRecording;

	private static GuiScreen previousScreen = null;

	public static void startRecording(RecraftRecording recording) {
		if (!ServerCheck.isOnCitybuild()) {
			labyBridge.notify("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild gestartet werden.");
			return;
		}

		DecompressRecorder.recording = recording;
		previousScreen = mc().currentScreen;

		display(ADDON_PREFIX + "Bitte klicke das Item an, das du dekomprimieren möchtest.");
		mc().displayGuiScreen(new GuiInventory(player()));
	}

	@EventListener
	private static void onGuiOpenEvent(GuiOpenEvent<?> event) {
	    if (previousScreen == null)
			return;

		if (event.gui instanceof GuiInventory || event.gui == previousScreen)
			return;

		event.cancel();
		mc().displayGuiScreen(previousScreen);
		previousScreen = null;
	}

	@EventListener
	private static void onAddItem(WindowClickEvent event) {
		if (previousScreen == null || event.itemStack == null)
			return;

		ItemStack stack = event.itemStack.copy();
		stack.stackSize = 0;
		recording.icon = stack;

		recording.actions.clear();
		recording.actions.add(new DecompressAction(new Ingredient(stack, 0)));
		mc().displayGuiScreen(previousScreen);
		previousScreen = null;
		event.cancel();
	}

}
