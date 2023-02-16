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

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Cancelable
public class WindowClickEvent extends Event {

	public final int windowId;
	public final int slotId;
	public final int mouseButtonClicked;
	public final int mode;
	public ItemStack itemStack = null;

	public WindowClickEvent(int windowId, int slotId, int mouseButtonClicked, int mode) {
		this.windowId = windowId;
		this.slotId = slotId;
		this.mouseButtonClicked = mouseButtonClicked;
		this.mode = mode;

		if (mc().currentScreen instanceof GuiContainer) {
			GuiContainer currentScreen = (GuiContainer) mc().currentScreen;
			List<Slot> slots = currentScreen.inventorySlots.inventorySlots;
			if (slotId >= slots.size() || slotId < 0)
				return;
			Slot slot = slots.get(slotId);
			itemStack = slot.getStack();
		}
	}

}
