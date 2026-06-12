/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.recipe;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Supplier;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceivedEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.features.item.recraft.Recraft;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftAction;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftRecording;
import dev.l3g7.griefer_utils.features.uncategorized.debug.RecraftLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S30PacketWindowItems;

import java.util.LinkedList;
import java.util.Queue;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static dev.l3g7.griefer_utils.features.item.recraft.recipe.ActionExecutor.FINISHED_EXECUTOR;

/**
 * @author Pleezon, L3g73
 */
public class RecipePlayer {

	private static Queue<RecipeAction> pendingActions;
	private static Supplier<Boolean> onFinish;

	private static ActionExecutor currentExecutor = FINISHED_EXECUTOR;
	private static int currentWindowId;
	private static PacketReceiveEvent<S2DPacketOpenWindow> lastReceiveEvent;

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

		RecraftLogger.log("Started recipe as " + (Recraft.playingSuccessor ? "successor" : "origin"));
		currentExecutor = FINISHED_EXECUTOR;

		if (Recraft.playingSuccessor)
			send("/rezepte");
		else
			player().sendChatMessage("/rezepte");
	}

	public static boolean isPlaying() {
		return pendingActions != null;
	}

	@EventListener
	private static void onPacketReceived(PacketReceivedEvent<S30PacketWindowItems> event) {
		if (currentWindowId != event.packet.func_148911_c())
			return;

		Queue<RecipeAction> pendingActions = RecipePlayer.pendingActions;

		if (pendingActions == null || pendingActions.isEmpty() || lastReceiveEvent == null)
			return;

		while (!pendingActions.isEmpty()) {
			RecipeAction nextAction = pendingActions.poll();
			ItemStack[] stacks = new ItemStack[36];
			System.arraycopy(event.packet.getItemStacks(), event.packet.getItemStacks().length - 36, stacks, 0, 36);

			if (nextAction.isAvailable(stacks, true)) {
				currentExecutor = new ActionExecutor(currentExecutor, nextAction);
				break;
			}

			if (nextAction.isAvailable(stacks, false)) {
				RecraftLogger.log("Skipping action (ItemSaver)");
				labyBridge.notify("§cAktion blockiert \u26A0", "§cDer Spezifische Item-Saver hat diese Aktion blockiert!");
			} else {
				RecraftLogger.log("Skipping action");
				labyBridge.notify("§eAktion übersprungen \u26A0", "§eDu hattest nicht genügend Zutaten im Inventar!");
			}
		}

		onPacketReceive(lastReceiveEvent);
	}

	@EventListener
	private static void onPacketReceive(PacketReceiveEvent<S2DPacketOpenWindow> event) {
		if (!"minecraft:container".equals(event.packet.getGuiId()))
			return;

		if (pendingActions == null)
			return;

		currentWindowId = event.packet.getWindowId();
		RecraftLogger.log("Received WindowItems: " + pendingActions.size() + " | " + currentWindowId);

		if (currentExecutor.isFinished()) {
			lastReceiveEvent = null;
			if (!pendingActions.isEmpty()) {
				lastReceiveEvent = event;
				return; // Next action will be triggered after a WindowItems packet
			}

			RecraftLogger.log("Closing window");
			pendingActions = null;
			TickScheduler.runNextClientTick(() -> {
				if (onFinish.get()) {
					mc().getNetHandler().addToSendQueue(new C0DPacketCloseWindow(currentWindowId));
					mc().addScheduledTask(player()::closeScreenAndDropStack);
				}
			});
			return;
		} else if (lastReceiveEvent == null) {
			if (!currentExecutor.onNewWindow()) {
				onPacketReceive(event);
				return;
			}
		}

		lastReceiveEvent = null;
		TickScheduler.runNextClientTick(() -> {
			if (currentWindowId == event.packet.getWindowId())
				executeAction(event.packet.getWindowId());
		});
	}

	private static void executeAction(int windowId) {
		currentExecutor.execute(windowId);

		TickScheduler.runAfterClientTicks(() -> {
			if (currentWindowId == windowId) {
				// Action failed, try again
				executeAction(windowId);
			}
		}, 10);
	}

	@EventListener
	private static void onCloseWindow(PacketSendEvent<C0DPacketCloseWindow> event) {
		if (pendingActions != null) {
			RecraftLogger.log("Sending close Window");
			pendingActions = null;
			currentWindowId = -1;
		}
	}

	@EventListener
	private static void onWindowClick(WindowClickEvent event) {
		if (pendingActions == null)
			return;

		LabyBridge.display(ADDON_PREFIX + "§cDas Abspielen wurde aufgrund einer manuellen Aktion abgebrochen.");
		pendingActions = null;
		currentWindowId = -1;
	}

}
