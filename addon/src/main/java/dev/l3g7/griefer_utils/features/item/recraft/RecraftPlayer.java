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
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.features.item.recraft.Recraft.Action;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.play.client.C0DPacketCloseWindow;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import static dev.l3g7.griefer_utils.features.item.recraft.Recraft.Mode.PLAYING;
import static dev.l3g7.griefer_utils.features.item.recraft.Recraft.Mode.RECORDING;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

/**
 * @author Pleezon
 */
class RecraftPlayer {

	private static Deque<Action> pendingActions;

	/**
	 * starts the player
	 */
	public static void play(Collection<Action> actions) {
		pendingActions = new LinkedList<>(actions);

		if (world() == null || !mc().inGameHasFocus || Recraft.currentMode == PLAYING)
			return;

		if (actions.isEmpty()) {
			MinecraftUtil.display(Constants.ADDON_PREFIX +" §cEs wurde kein \"/rezepte\"-Aufruf aufgezeichnet.");
			return;
		}

		Recraft.currentMode = PLAYING;
		MinecraftUtil.send("/rezepte");
	}

	private static void stop() {
		Recraft.currentMode = RECORDING;
	}

	@EventListener
	private static void onGuiOpen(GuiOpenEvent<GuiChest> event) {
		if (Recraft.currentMode == RECORDING)
			return;

		if (pendingActions.isEmpty()) {
			closeGui();
			stop();
			return;
		}

		if (!pendingActions.peek().isApplicableTo(event.gui)) {
			stop();
			return;
		}

		TickScheduler.runAfterRenderTicks(() -> {
			if (!pendingActions.isEmpty())
				pendingActions.poll().execute(event.gui);

			if (pendingActions.isEmpty())
				closeGui();
		}, 1);
	}

	@EventListener
	private static void onCloseWindow(PacketSendEvent<C0DPacketCloseWindow> event) {
		stop();
	}

	private static void closeGui() {
		TickScheduler.runAfterClientTicks(() -> player().closeScreen(), 2);
	}

}
