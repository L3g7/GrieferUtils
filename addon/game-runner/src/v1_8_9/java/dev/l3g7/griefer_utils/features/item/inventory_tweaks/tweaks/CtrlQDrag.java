/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.inventory_tweaks.tweaks;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.DrawScreenEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.lwjgl.input.Keyboard;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class CtrlQDrag extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Strg + Q verbessern")
		.description("Ermöglicht das schnelle Droppen von Items durch Strg + Q + Hovern (so wie in 1.12+).")
		.icon("hopper");

	private static Slot previousTheSlot;

	@EventListener
	private void onDrawScreen(DrawScreenEvent event) {
		if (!(event.gui instanceof GuiContainer gc))
			return;

		Slot theSlot = Reflection.get(gc, "theSlot");
		if (theSlot == previousTheSlot)
			return;

		previousTheSlot = theSlot;
		if (theSlot == null || !theSlot.getHasStack())
			return;

		if (!Keyboard.isKeyDown(mc().gameSettings.keyBindDrop.getKeyCode()) || !GuiScreen.isCtrlKeyDown())
			return;

		mc().playerController.windowClick(gc.inventorySlots.windowId, theSlot.slotNumber, 1, 4, mc().thePlayer);
	}

}
