/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.recipe;

import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftAction.Ingredient;
import dev.l3g7.griefer_utils.features.uncategorized.debug.RecraftLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.features.item.recraft.recipe.ActionState.*;

class ActionExecutor {

	public static ActionExecutor FINISHED_EXECUTOR = new ActionExecutor();

	final RecipeAction current;
	final RecipeAction target;
	int shortcut = -1;

	private ActionState state = CATEGORY;

	private ActionExecutor() {
		current = target = new RecipeAction();
		state = CRAFT;
	}

	public ActionExecutor(ActionExecutor previous, RecipeAction target) {
		this.target = target;

		if (previous == FINISHED_EXECUTOR) {
			this.current = new RecipeAction();
			return;
		}

		this.current = previous.target.copy();
		if (current.page != target.page || current.slot != target.slot)
			state = BACK_TO_PAGE;
		else if (current.category != target.category)
			state = BACK_TO_CATEGORY;
		else
			state = VARIANT;
	}

	/**
	 * @return whether the recording can be played
	 */
	public boolean onNewWindow() {
		if (!state.canContinue(this))
			return true;

		ActionState startState = state;
		switch (state) {
			case BACK_TO_CATEGORY -> state = CATEGORY;
			case BACK_TO_PAGE -> {
				if (current.category != target.category)
					state = BACK_TO_CATEGORY;
				else
					attemptShortcut((current.page == target.page) ? SLOT : PAGE);
			}
			case CATEGORY -> {
				attemptShortcut((current.page == target.page) ? SLOT : PAGE);
				if (state != SHORTCUT && target.slot == -1) {
					RecraftLogger.log("Forced shortcut failed");
					labyBridge.notify("§eAktion übersprungen \u26A0", "§eDu hattest nicht genügend Zutaten im Inventar!");
					state = CRAFT;
					return false;
				}
			}
			case SHORTCUT, SLOT -> state = current.variant == target.variant ? CRAFT : VARIANT;
			default -> state = ActionState.values()[state.ordinal() + 1];
		}

		RecraftLogger.log("Continued from " + startState + " to " + state);
		return true;
	}

	private void attemptShortcut(ActionState fallback) {
		int shortcut = calculateShortcut();
		if (shortcut == -1) {
			state = fallback;
			return;
		}

		state = SHORTCUT;
		this.shortcut = shortcut + (shortcut < 9 ? 81 : 45);
	}

	public boolean isFinished() {
		return state == CRAFT;
	}

	public void execute(int windowId) {
		int slot = state.getSlot(this);
		RecraftLogger.log("Executing " + state + " (-> " + slot + ")");
		mc().getNetHandler().addToSendQueue(new C0EPacketClickWindow(windowId, Math.abs(slot), 0, slot < 0 ? 1 : 0, null, (short) 0));
	}

	private int calculateShortcut() {
		if (target.result == null)
			return -1;

		int maxVariantDiff = Math.abs(current.page - target.page) + Math.abs(current.variant - target.variant);

		int bestSlot = -1;
		int bestVariant = 1;
		int bestDiff = 999;

		ItemStack[] inv = player().inventory.mainInventory;
		for (int i = 0; i < inv.length; i++) {
			if (!target.result.itemEquals(Ingredient.fromItemStack(inv[i])))
				continue;

			if (target.category == 1) // Compression doesn't matter when crafting
				return i;

			int variant = ItemUtil.getCompressionLevel(inv[i]) + 1;
			if (variant == 8)
				continue;

			int newDiff = Math.abs(variant - target.variant);
			if (newDiff == 0) {
				current.variant = variant;
				return i;
			}

			if (newDiff < bestDiff) {
				bestDiff = newDiff;
				bestSlot = i;
				bestVariant = variant;
			}
		}

		current.variant = bestVariant;
		return maxVariantDiff > bestDiff ? bestSlot : -1;
	}

}
