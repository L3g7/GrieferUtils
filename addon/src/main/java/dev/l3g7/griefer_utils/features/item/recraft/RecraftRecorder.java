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

package dev.l3g7.griefer_utils.features.item.recraft;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.item.recraft.Recraft.Action;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.play.client.C0EPacketClickWindow;

import java.util.LinkedList;

import static dev.l3g7.griefer_utils.features.item.recraft.Recraft.Mode.PLAYING;

/**
 * @author Pleezon
 */
class RecraftRecorder {

	static LinkedList<Action> actions = new LinkedList<>();

	private static int currentGuiID = -1;

	@EventListener
	private static void onGuiOpen(GuiScreenEvent.GuiOpenEvent<?> event) {
		if (Recraft.currentMode == PLAYING)
			return;

		if (!(event.gui instanceof GuiChest)) {
			currentGuiID = -1;
			return;
		}

		GuiChest chest = (GuiChest) event.gui;
		int id = Recraft.getMenuID(chest);
		if (id == 0)
			actions.clear();

		currentGuiID = id;
	}

	@EventListener
	private static void onSendClick(PacketEvent.PacketSendEvent<C0EPacketClickWindow> event) {
		if (Recraft.currentMode == PLAYING || currentGuiID == -1)
			return;

		C0EPacketClickWindow packet = event.packet;
		if (packet.getClickedItem() == null || packet.getClickedItem().getDisplayName().equals("§7"))
			return;

		actions.add(new Action(currentGuiID, packet.getSlotId(), packet.getClickedItem(), packet.getMode(), packet.getUsedButton()));
	}

}