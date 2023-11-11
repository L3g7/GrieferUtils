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
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.event.events.WindowClickEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.play.client.C0DPacketCloseWindow;

import java.util.Deque;
import java.util.LinkedList;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

/**
 * @author Pleezon, L3g73
 */
class RecraftPlayer {

	private static Deque<Action> pendingActions;
	private static boolean closeGui = false;
	private static boolean executingAction = false;

	public static void play(RecraftRecording recording) {
		if (world() == null || !mc().inGameHasFocus)
			return;

		pendingActions = new LinkedList<>(recording.actions);

		player().sendChatMessage("/rezepte");
	}

	public static boolean isPlaying() {
		return pendingActions != null;
	}

	@EventListener
	private static void onGuiOpen(GuiOpenEvent<GuiChest> event) {
		if (closeGui) {
			TickScheduler.runAfterRenderTicks(() -> player().closeScreen(), 1);
			closeGui = false;
			return;
		}

		if (pendingActions == null)
			return;

		if (pendingActions.isEmpty()) {
			closeGui = true;
			pendingActions = null;
			return;
		}

		TickScheduler.runAfterRenderTicks(() -> {
			if (!pendingActions.isEmpty()) {
				executingAction = true;
				pendingActions.poll().execute(event.gui);
				executingAction = false;
			}

			if (pendingActions.isEmpty())
				closeGui = true;
		}, 1);
	}

	@EventListener
	private static void onCloseWindow(PacketSendEvent<C0DPacketCloseWindow> event) {
		pendingActions = null;
	}

	@EventListener
	private static void onWindowClick(WindowClickEvent event) {
		if (pendingActions == null || executingAction)
			return;

		display(Constants.ADDON_PREFIX + "§cDas Abspielen wurde aufgrund einer manuellen Aktion abgebrochen.");
		pendingActions = null;
	}

}
