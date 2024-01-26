/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.features.item.recraft;

import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.Action.Ingredient;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C0EPacketClickWindow;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;

/**
 * @author Pleezon, L3g73
 */
class RecraftRecorder {

	private static RecraftRecording recording = Recraft.tempRecording;
	private static GuiScreen previousScreen = null;
	private static boolean addedIcon = false;
	private static boolean isMenuOpen = false;
	private static boolean executedCommand = false;

	public static void startRecording(RecraftRecording recording) {
		if (!ServerCheck.isOnCitybuild()) {
			LabyBridge.labyBridge.notifyMildError("Aufzeichnungen können nur auf einem Citybuild gestartet werden.");
			return;
		}

		RecraftRecorder.recording = recording;
		previousScreen = mc().currentScreen;
		addedIcon = false;
		executedCommand = true;
		send("/rezepte");
	}

	@EventListener
	private static void onMessageSend(PacketSendEvent<C01PacketChatMessage> event) {
		String lowerMsg = event.packet.getMessage().toLowerCase();
		if (!RecraftPlayer.isPlaying() && (lowerMsg.equals("/rezepte") || lowerMsg.startsWith("/rezepte ")))
			executedCommand = true;
	}

	@EventListener
	private static void onGuiOpen(GuiOpenEvent<?> event) {
		if (RecraftPlayer.isPlaying())
			return;

		if (event.gui instanceof GuiChest) {
			if (executedCommand) {
				isMenuOpen = true;
				recording.actions.clear();
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
	private static void onSendClick(PacketSendEvent<C0EPacketClickWindow> event) {
		if (!isMenuOpen)
			return;

		C0EPacketClickWindow packet = event.packet;
		if (packet.getClickedItem() == null || packet.getClickedItem().getDisplayName().equals("§7"))
			return;

		boolean isCrafting = "§7Klicke, um dieses Rezept herzustellen.".equals(ItemUtil.getLoreAtIndex(packet.getClickedItem(), 0));
		if (!addedIcon && isCrafting) {
			ItemStack targetStack = player().openContainer.getSlot(25).getStack().copy();
			targetStack.stackSize = ItemUtil.getCompressionLevel(targetStack);
			recording.mainSetting.icon(targetStack);
			addedIcon = true;
		}

		if (packet.getSlotId() > 53) {
			Ingredient ingredient = Ingredient.fromItemStack(packet.getClickedItem());
			if (ingredient != null)
				recording.actions.add(new Action(ingredient));
			return;
		}

		if (!isCrafting || player().openContainer.windowId != packet.getWindowId()) {
			recording.actions.add(new Action(packet.getSlotId(), null));
			return;
		}

		Ingredient[] ingredients = new Ingredient[9];
		for (int i = 0; i < 9; i++) {
			ItemStack stack = player().openContainer.getSlot(10 + i % 3 + i / 3 * 9).getStack();
			if (stack != null && !stack.getDisplayName().equals("§7"))
				ingredients[i] = Ingredient.fromItemStack(stack);
		}

		int slot = packet.getSlotId();
		if (packet.getMode() == 1)
			slot = -slot;

		recording.actions.add(new Action(slot, Action.SizedIngredient.fromIngredients(ingredients)));
	}

}