/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * This file has been modified by itzW0lf.
 */

package dev.l3g7.griefer_utils.features.item.inventory_tweaks;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.DrawScreenEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.lwjgl.input.Mouse;

import java.util.HashSet;
import java.util.Set;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class ShiftDrag extends Feature {

	private final Set<Integer> processedSlots = new HashSet<>();
	private long lastClick = 0;
	private static final long CLICK_DELAY_MS = 75;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Shift-Ziehen")
		.description("Halte Shift + linke Maustaste gedrückt und ziehe über Items, um alle überfahrenen Items schnell zu verschieben (wie Mouse Tweaks).")
		.icon("arrows_up")
		.since("2.5.0");

	@EventListener
	private void onDrawScreen(DrawScreenEvent event) {
		if (!(event.gui instanceof GuiContainer gc)) {
			processedSlots.clear();
			return;
		}

		if (!Mouse.isButtonDown(0) || !GuiScreen.isShiftKeyDown()) {
			processedSlots.clear();
			return;
		}

		Slot hovered = Reflection.get(gc, "theSlot");
		if (hovered == null || !hovered.getHasStack() || processedSlots.contains(hovered.slotNumber))
			return;

		long now = System.currentTimeMillis();
		if (now - lastClick < CLICK_DELAY_MS)
			return;

		processedSlots.add(hovered.slotNumber);
		lastClick = now;
		mc().playerController.windowClick(gc.inventorySlots.windowId, hovered.slotNumber, 0, 1, mc().thePlayer);
	}

}
