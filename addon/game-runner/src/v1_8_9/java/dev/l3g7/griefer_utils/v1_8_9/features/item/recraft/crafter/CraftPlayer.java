/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.crafter;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.Recraft;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.RecraftAction;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.RecraftAction.Ingredient;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.RecraftRecording;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import dev.l3g7.griefer_utils.v1_8_9.misc.TickScheduler;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S30PacketWindowItems;

import java.util.*;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.crafter.CraftPlayer.State.*;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;

/**
 * @author Pleezon, L3g73
 */
public class CraftPlayer {

	private static RecraftRecording currentRecording;
	private static Queue<CraftAction> pendingActions;
	private static int failedActions = 0;
	private static boolean firstPlay = false;

	private static Ingredient[] ingredients;
	private static int[] sourceIds; // crafting slot id -> hotbar id
	private static int[] hotbarSourceIds; // hotbar id -> inventory slot id
	private static Integer windowId;
	private static State state;
	private static Object resyncReference;

	private static Supplier<Boolean> onFinish;

	public static boolean isPlaying() {
		return state != IDLE;
	}

	public static void play(RecraftRecording recording) {
		windowId = null;
		play(recording, recording::playSuccessor, true);
	}

	/**
	 * @return whether the recording was started successfully
	 */
	public static boolean play(RecraftRecording recording, Supplier<Boolean> onFinish, boolean reset) {
		pendingActions = null;
		if (world() == null || !mc().inGameHasFocus)
			return false;

		if (!ServerCheck.isOnCitybuild()) {
			labyBridge.notify("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild abgespielt werden.");
			return false;
		}

		if (recording.actions.isEmpty()) {
			labyBridge.notify("§e§lFehler \u26A0", "§eDiese Aufzeichnung ist leer!");
			return false;
		}

		CraftPlayer.onFinish = onFinish;
		if (reset) {
			windowId = null;
			state = WAITING_FOR_GUI;
			firstPlay = true;
			if (Recraft.playingSuccessor)
				MinecraftUtil.send("/craft");
			else
				player().sendChatMessage("/craft");
		}

		playRecording(currentRecording = recording);
		return true;
	}

	private static void playRecording(RecraftRecording recording) {
		failedActions = 0;
		pendingActions = new LinkedList<>();
		for (RecraftAction action : recording.actions)
			pendingActions.add((CraftAction) action);
	}

	private static void startAction() {
		state = IDLE;
		while (tryStartAction()) {
			if (pendingActions != null) {
				failedActions++;
				if (firstPlay)
					labyBridge.notify("§eAktion übersprungen \u26A0", "Du hattest nicht genügend Zutaten im Inventar!");
				continue;
			}

			if (failedActions != currentRecording.actions.size() && currentRecording.craftAll.get()) {
				firstPlay = false;
				playRecording(currentRecording);
				startAction();
				return;
			}

			if (onFinish.get()) {
				player().closeScreen();
				player().openContainer.putStackInSlot(1, null);
				pendingActions = null;
				windowId = null;
				return;
			}
		}
	}

	private static boolean tryStartAction() {
		if (pendingActions == null || pendingActions.isEmpty()) {
			pendingActions = null;
			return true;
		}

		CraftAction action = pendingActions.poll();
		hotbarSourceIds = null;

		if ((sourceIds = action.getSlotsFromHotbar()) != null)
			state = INTO_CRAFTING;
		else if ((sourceIds = action.getSlotsFromInventory()) != null)
			state = INTO_HOTBAR;
		else
			return true;

		ingredients = action.ingredients;
		executeClicks();
		return false;
	}

	private static void executeClicks() {
		int clicks = 0;

		if (state == INTO_HOTBAR) {
			// Calculate where the items can go
			if (hotbarSourceIds == null) {
				hotbarSourceIds = new int[9];
				Arrays.fill(hotbarSourceIds, -1);

				List<Integer> blockedSlots = new ArrayList<>(9);
				for (int targetId : sourceIds)
					if (targetId < 9)
						blockedSlots.add(targetId);

				boolean replaceItems = false;
				for (int i = 0; i < sourceIds.length; i++) {
					if (sourceIds[i] < 9)
						continue;

					int slotId = -1;
					findLoop:
					for (int attempt = 0; attempt < 2; attempt++) {
						for (int index = 8; index >= 0; index--) { // Iterate backwards through the slots, since most important items often are at the start
							if (!blockedSlots.contains(index) && (replaceItems || player().inventory.getStackInSlot(index) == null)) {
								blockedSlots.add(slotId = index);
								break findLoop;
							}
						}

						replaceItems = true;
					}

					hotbarSourceIds[slotId] = sourceIds[i] + 1;
					sourceIds[i] = slotId;
				}
			}

			for (int i = 0; i < hotbarSourceIds.length; i++) {
				if (hotbarSourceIds[i] == -1)
					continue;

				click(windowId, hotbarSourceIds[i], i, clicks++);
			}
		}

		if (state == INTO_CRAFTING) {
			for (int i = 0; i < sourceIds.length; i++) {
				if (sourceIds[i] == -1)
					continue;

				click(windowId, i + 1, sourceIds[i], clicks++);
			}
		}

		TickScheduler.runAfterClientTicks(() -> forceResync(state, null), clicks);
	}

	@EventListener
	private static void onGuiOpen(PacketReceiveEvent<S2DPacketOpenWindow> event) {
		if (state == WAITING_FOR_GUI && event.packet.getGuiId().equals("minecraft:crafting_table"))
			windowId = event.packet.getWindowId();
	}

	/*
	 *	For some reason, merging the delay into this method breaks everything :/
	 */
	private static void forceResync(State state, Object reference) {
		if (reference == null)
			resyncReference = reference = new Object();

		if (CraftPlayer.state != state
			|| state.ordinal() < INTO_HOTBAR.ordinal()
			|| windowId == null
			|| resyncReference != reference)
			return;

		mc().getNetHandler().addToSendQueue(new C0EPacketClickWindow(windowId, 0, -1, 0, new ItemStack(Blocks.dirt), (short) -999));
		mc().getNetHandler().addToSendQueue(new C0FPacketConfirmTransaction(windowId, (short) -999, true));

		Object finalReference = reference;
		TickScheduler.runAfterClientTicks(() -> forceResync(state, finalReference), 60);
	}

	@EventListener
	private static void onPacketReceive(PacketReceiveEvent<S30PacketWindowItems> event) {
		if (state == IDLE || windowId == null || event.packet.func_148911_c() != windowId)
			return;

		if (state == WAITING_FOR_GUI) {
			state = INTO_HOTBAR;
			TickScheduler.runAfterRenderTicks(CraftPlayer::startAction, 1);
			return;
		}

		if (resyncReference == null)
			return;

		resyncReference = null;

		if (state == FINISHED) {
			TickScheduler.runAfterClientTicks(CraftPlayer::startAction, 1);
			return;
		}

		ItemStack[] stacks = event.packet.getItemStacks();
		int fails = 0;

		if (state == GRABBING_RESULT) {
			if (stacks[0] != null)
				click(event.packet.func_148911_c(), 0, 0, ++fails * 2);
		} else if (state == INTO_CRAFTING) {
			for (int i = 0; i < 9; i++)
				if (sourceIds[i] != -1 && !Ingredient.check(ingredients[i], stacks[i + 1]))
					click(event.packet.func_148911_c(), i + 1, sourceIds[i], ++fails * 2);
		} else if (state == INTO_HOTBAR) {
			for (int i = 0; i < hotbarSourceIds.length; i++) {
				if (hotbarSourceIds[i] == -1)
					continue;

				for (int j = 0; j < sourceIds.length; j++) {
					if (sourceIds[j] != i)
						continue;

					if (!Ingredient.check(ingredients[j], stacks[i + 37]))
						click(event.packet.func_148911_c(), hotbarSourceIds[i], i, ++fails * 2);
					break;
				}
			}
		}

		if (fails == 0 && state == INTO_CRAFTING && stacks[0] == null)
			fails++;

		if (fails != 0) {
			TickScheduler.runAfterClientTicks(() -> forceResync(state, null), ++fails * 2);
			return;
		}

		state = State.values()[state.ordinal() + 1];

		if (state == FINISHED) {
			// Resync one last time
			TickScheduler.runAfterClientTicks(() -> forceResync(state, null), 1);
			return;
		}

		if (state == GRABBING_RESULT) {
			click(event.packet.func_148911_c(), 0, 0, 1);
			TickScheduler.runAfterClientTicks(() -> forceResync(state, null), 2);
			return;
		}

		if (state == INTO_CRAFTING)
			TickScheduler.runAfterClientTicks(CraftPlayer::executeClicks, 1);
	}

	private static void click(int windowId, int slotId, int button, int delay) {
		int mode = state == GRABBING_RESULT ? 1 : 2;
		TickScheduler.runAfterClientTicks(() -> mc().playerController.windowClick(windowId, slotId, button, mode, player()), delay);
	}

	@EventListener
	private static void onWindowClose(GuiOpenEvent<?> event) {
		if (state == IDLE || event.gui instanceof GuiCrafting)
			return;

		state = IDLE;
	}

	enum State {

		IDLE,
		WAITING_FOR_GUI,
		INTO_HOTBAR,
		INTO_CRAFTING,
		GRABBING_RESULT,
		FINISHED

	}

}
