/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.recipe;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.features.item.recraft.Recraft;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftAction;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftRecording;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;

import java.util.LinkedList;
import java.util.Queue;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

/**
 * @author Pleezon, L3g73
 */
public class RecipePlayer {

	private static Queue<RecipeAction> pendingActions;
	private static boolean closeGui = false;
	private static RecipeAction actionBeingExecuted = null;
	private static Supplier<Boolean> onFinish;

	public static void play(RecraftRecording recording) {
		play(recording, recording::playSuccessor);
	}

	public static void play(RecraftRecording recording, Supplier<Boolean> onFinish) {
		if (world() == null || !mc().inGameHasFocus)
			return;

		if (!ServerCheck.isOnCitybuild()) {
			labyBridge.notify("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild abgespielt werden.");
			return;
		}

		if (recording.actions().isEmpty()) {
			labyBridge.notify("§e§lFehler \u26A0", "§eDiese Aufzeichnung ist leer!");
			return;
		}

		RecipePlayer.onFinish = onFinish;
		pendingActions = new LinkedList<>();
		for (RecraftAction action : recording.actions())
			pendingActions.add((RecipeAction) action);

		if (Recraft.playingSuccessor)
			send("/rezepte");
		else
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
			closeGui = false;
			TickScheduler.runAfterClientTicks(() -> {
				if (onFinish.get()) {
					mc().getNetHandler().addToSendQueue(new C0DPacketCloseWindow(event.packet.getWindowId()));
					mc().addScheduledTask(player()::closeScreenAndDropStack);
				}
			}, 1);
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
			pendingActions = null;
			TickScheduler.runAfterClientTicks(() -> {
				player().closeScreen();
				onFinish.get();
			}, 1);
		} else {
			executeAction(pendingActions.poll(), windowId, false);
		}

		return true;
	}

	@EventListener
	private static void onCloseWindow(PacketSendEvent<C0DPacketCloseWindow> event) {
		pendingActions = null;
	}

	@EventListener
	private static void onWindowClick(WindowClickEvent event) {
		if (pendingActions == null || actionBeingExecuted != null)
			return;

		LabyBridge.display(ADDON_PREFIX + "§cDas Abspielen wurde aufgrund einer manuellen Aktion abgebrochen.");
		pendingActions = null;
	}

}
