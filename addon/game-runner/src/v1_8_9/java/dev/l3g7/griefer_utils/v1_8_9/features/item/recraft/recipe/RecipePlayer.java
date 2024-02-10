/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.recipe;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.v1_8_9.events.WindowClickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.RecraftAction;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.RecraftRecording;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import dev.l3g7.griefer_utils.v1_8_9.misc.TickScheduler;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;

import java.util.LinkedList;
import java.util.Queue;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;

/**
 * @author Pleezon, L3g73
 */
public class RecipePlayer {

	private static Queue<RecipeAction> pendingActions;
	private static boolean closeGui = false;
	private static RecipeAction actionBeingExecuted = null;

	public static void play(RecraftRecording recording) {
		if (world() == null || !mc().inGameHasFocus)
			return;

		if (!ServerCheck.isOnCitybuild()) {
			labyBridge.notify("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild abgespielt werden.");
			return;
		}

		if (recording.actions.isEmpty()) {
			labyBridge.notify("§e§lFehler \u26A0", "§eDiese Aufzeichnung ist leer!");
			return;
		}

		pendingActions = new LinkedList<>();
		for (RecraftAction action : recording.actions)
			pendingActions.add((RecipeAction) action);

		player().sendChatMessage("/rezepte");
	}

	public static boolean isPlaying() {
		return pendingActions != null;
	}

	@EventListener
	private static void onPacketReceive(PacketReceiveEvent<S2DPacketOpenWindow> event) {
		if (!("minecraft:container".equals(event.packet.getGuiId())))
			return;

		actionBeingExecuted = null;
		if (closeGui) {
			event.cancel();
			mc().getNetHandler().addToSendQueue(new C0DPacketCloseWindow(event.packet.getWindowId()));
			mc().addScheduledTask(player()::closeScreenAndDropStack);
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
			if (!pendingActions.isEmpty())
				executeAction(pendingActions.poll(), event.packet.getWindowId(), false);

			if (pendingActions != null && pendingActions.isEmpty())
				closeGui = true;
		}, 1);
	}

	private static void executeAction(RecipeAction action, int windowId, boolean hasSucceeded) {
		actionBeingExecuted = action;
		if (handleErrors(action.execute(windowId, hasSucceeded), windowId, hasSucceeded))
			return;

		TickScheduler.runAfterClientTicks(() -> {
			if (actionBeingExecuted == action) {
				// Action failed, try again
				executeAction(action, windowId, true);
			}
		}, 2);
	}

	private static boolean handleErrors(Boolean result, int windowId, boolean hasSucceeded) {
		// Success
		if (result == Boolean.TRUE)
			return false;

		// Action failed
		if (result == Boolean.FALSE) {
			TickScheduler.runAfterClientTicks(player()::closeScreen, 1);
			pendingActions = null;
		}

		// Action was skipped
		if (pendingActions == null || hasSucceeded)
			return true;

		if (pendingActions.isEmpty()) {
			TickScheduler.runAfterClientTicks(player()::closeScreen, 1);
			pendingActions = null;
		} else {
			executeAction(pendingActions.poll(), windowId, false);
		}

		return true;
	}

	@EventListener
	private static void onCloseWindow(PacketEvent.PacketSendEvent<C0DPacketCloseWindow> event) {
		pendingActions = null;
	}

	@EventListener
	private static void onWindowClick(WindowClickEvent event) {
		if (pendingActions == null || actionBeingExecuted != null)
			return;

		display(Constants.ADDON_PREFIX + "§cDas Abspielen wurde aufgrund einer manuellen Aktion abgebrochen.");
		pendingActions = null;
	}

}
