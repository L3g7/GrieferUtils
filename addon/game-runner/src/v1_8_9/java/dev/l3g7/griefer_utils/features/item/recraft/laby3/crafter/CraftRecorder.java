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

package dev.l3g7.griefer_utils.features.item.recraft.laby3.crafter;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.features.item.recraft.laby3.Recraft;
import dev.l3g7.griefer_utils.features.item.recraft.laby3.RecraftAction.Ingredient;
import dev.l3g7.griefer_utils.features.item.recraft.laby3.RecraftRecording;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C01PacketChatMessage;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.features.item.recraft.laby3.RecraftRecording.RecordingMode.CRAFT;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

@ExclusiveTo(LABY_3)
public class CraftRecorder {

	private static RecraftRecording recording = Recraft.tempRecording;
	private static GuiScreen previousScreen = null;
	private static boolean addedIcon = false;
	private static boolean isMenuOpen = false;
	private static boolean executedCommand = false;

	public static void startRecording(RecraftRecording recording) {
		if (!ServerCheck.isOnCitybuild()) {
			LabyBridge.labyBridge.notify("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild gestartet werden.");
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
//			recording.mainSetting.icon(targetStack);
			addedIcon = true;
		}


		Ingredient[] ingredients = new Ingredient[9];
		for (int i = 0; i < 9; i++)
			ingredients[i] = Ingredient.fromItemStack(player().openContainer.getSlot(i + 1).getStack());

		recording.actions.add(new CraftAction(ingredients));
	}

}