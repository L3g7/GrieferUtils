/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.events.TickEvent.RenderTickEvent;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

public class GuiModifyItemsEvent extends Event.TypedEvent<GuiModifyItemsEvent> {

	@EventListener(priority = Priority.LOW)
	private static void onRenderTick(RenderTickEvent event) {
		GuiScreen currentScreen = mc().currentScreen; // Account for concurrency
		if (currentScreen instanceof GuiChest && player() != null)
			new GuiModifyItemsEvent(MinecraftUtil.getGuiChestTitle(), ((GuiChest) currentScreen).inventorySlots).fire();
	}

	public GuiModifyItemsEvent(String title, Container container) {
		this.title = title;
		this.container = container;
	}

	private final String title;
	private final Container container;


	public ItemStack getItem(int slot) {
		return container.getSlot(slot).getStack();
	}

	public void setItem(int slot, ItemStack itemStack) {
		container.putStackInSlot(slot, itemStack);
	}

	public String getTitle() {
		return title;
	}

}
