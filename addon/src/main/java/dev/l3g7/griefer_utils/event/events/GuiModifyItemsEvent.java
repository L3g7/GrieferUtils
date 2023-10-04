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
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

public class GuiModifyItemsEvent extends Event.TypedEvent<GuiModifyItemsEvent> {

	private static final HashMap<Integer, GuiModifyItemsEvent> guiMap = new HashMap<>();

	@EventListener(priority = Priority.LOW)
	private static void onPacket(PacketEvent.PacketReceiveEvent<Packet<?>> event) {
		if (player() == null)
			return;

		if (event.packet instanceof S2DPacketOpenWindow) {
			S2DPacketOpenWindow packet = (S2DPacketOpenWindow) event.packet;
			if (packet.getGuiId().equals("minecraft:container"))
				guiMap.put(packet.getWindowId(), new GuiModifyItemsEvent(packet.getWindowId(), packet.getWindowTitle().getFormattedText()));

			return;
		}

		if (event.packet instanceof S30PacketWindowItems) {
			S30PacketWindowItems packet = ((S30PacketWindowItems) event.packet);
			GuiModifyItemsEvent guiModifyItemsEvent = guiMap.get(packet.func_148911_c());
			if (guiModifyItemsEvent == null)
				return;

			guiModifyItemsEvent.itemStacks = Arrays.asList(packet.getItemStacks());
			if (fireEvent(guiModifyItemsEvent))
				event.cancel();
		}

		if (!(event.packet instanceof S2FPacketSetSlot))
			return;

		S2FPacketSetSlot packet = (S2FPacketSetSlot) event.packet;

		GuiModifyItemsEvent guiModifyItemsEvent = guiMap.get(packet.func_149175_c());
		if (guiModifyItemsEvent == null)
			return;

		guiModifyItemsEvent.itemStacks.set(packet.func_149173_d(), packet.func_149174_e());
		if (fireEvent(guiModifyItemsEvent))
			event.cancel();
	}

	private static boolean fireEvent(GuiModifyItemsEvent event) {
		synchronized (event) {
			int hash = ItemUtil.hashcode(event.itemStacks);

			event.fire();
			if (ItemUtil.hashcode(event.itemStacks) == hash)
				return false;

			List<ItemStack> stacks = new ArrayList<>(event.itemStacks);
			mc().addScheduledTask(() -> {
				if (event.windowId != player().openContainer.windowId)
					return;

				Container openContainer = player().openContainer;

				ItemStack[] inv = Reflection.get(openContainer.getSlot(0).inventory, "inventoryContents");
				ItemStack[] playerInv = Reflection.get(player().inventory, "mainInventory");

				for (int i = 0; i < stacks.size(); i++) {
					Slot slot = openContainer.getSlot(i);
					ItemStack stack = stacks.get(i);
					int slotIndex = Reflection.get(slot, "slotIndex");
					(slot.inventory == player().inventory ? playerInv : inv)[slotIndex] = stack;
					slot.inventory.markDirty();
				}
			});

			return true;
		}
	}

	private final int windowId;
	private final String title;
	public List<ItemStack> itemStacks;

	public GuiModifyItemsEvent(int windowId, String title) {
		this.windowId = windowId;
		this.title = title;
	}

	public List<ItemStack> getItems() {
		return itemStacks;
	}

	public ItemStack getItem(int slot) {
		return itemStacks.get(slot);
	}

	public List<ItemStack> getInventory() {
		int size = itemStacks.size();
		return itemStacks.subList(size - 36, size);
	}

	public void setItem(int slot, ItemStack itemStack) {
		itemStacks.set(slot, itemStack);
	}

	public String getTitle() {
		return title;
	}

}
