/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.event.events;

import dev.l3g7.griefer_utils.core.event_bus.Event;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.event_bus.Priority;
import dev.l3g7.griefer_utils.event.events.TickEvent.RenderTickEvent;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

public class GuiModifyItemsEvent extends Event.TypedEvent<GuiModifyItemsEvent> {

	@EventListener(priority = Priority.LOW)
	private static void onRenderTick(RenderTickEvent event) {
		if (!(mc().currentScreen instanceof GuiChest) || player() == null)
			return;

		GuiChest currentScreen = (GuiChest) mc().currentScreen;
		new GuiModifyItemsEvent(MinecraftUtil.getGuiChestTitle(), currentScreen.inventorySlots).fire();
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
