/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.decompressor;

import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftAction.Ingredient;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftRecording;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftAction;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftPlayer;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static dev.l3g7.griefer_utils.features.item.recraft.RecraftBridge.recraftBridge;

public class DecompressPlayer {

	private static final RecraftRecording craftRecording = recraftBridge.createEmptyRecording();
	private static RecraftRecording recording;

	/**
	 * @return whether the recording was started successfully
	 */
	public static boolean play(RecraftRecording recording) {
		if (world() == null || !mc().inGameHasFocus)
			return false;

		if (!ServerCheck.isOnCitybuild()) {
			labyBridge.notify("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild abgespielt werden.");
			return false;
		}

		if (recording.actions().isEmpty()) {
			labyBridge.notify("§e§lFehler \u26A0", "§eDiese Aufzeichnung ist leer!");
			return false;
		}

		DecompressPlayer.recording = recording;
		Ingredient ingredient = ((DecompressAction) recording.actions().get(0)).ingredient;
		return craft(ingredient);
	}

	private static boolean craft(Ingredient ingredient) {
		ItemStack[] inv = player().inventory.mainInventory;

		int freeSlots = 0;

		for (ItemStack stack : inv)
			if (stack == null)
				freeSlots++;

		int slot = getSlotWithLowestCompression(ingredient, freeSlots);
		if (slot < 0) {
			if (slot == -1)
				labyBridge.notify("§e§lFehler \u26A0", "Du hast nicht genügend Platz im Inventar!");

			recording.playSuccessor();
			return true;
		}

		ItemStack stack = player().inventory.mainInventory[slot];
		return startCrafting(new PredeterminedIngredient(stack, slot));
	}

	private static boolean startCrafting(PredeterminedIngredient ingredient) {
		Ingredient[] ingredients = new Ingredient[9];
		ingredients[0] = ingredient;

		craftRecording.actions().clear();
		craftRecording.actions().add(new CraftAction(ingredients));

		return CraftPlayer.play(craftRecording, () -> craft(ingredient), false, true);
	}

	private static int getSlotWithLowestCompression(Ingredient ingredient, int freeSlots) {
		int compression = 8;
		int slot = -1;
		int placeChecksFailed = 0;
		ItemStack[] inv = player().inventory.mainInventory;

		for (int i = 0; i < inv.length; i++) {
			Ingredient slotIngredient = Ingredient.fromItemStack(inv[i]);
			if (!ingredient.itemEquals(slotIngredient))
				continue;

			if (slotIngredient.compression >= compression)
				continue;

			if (slotIngredient.compression == 0)
				continue;

			if (getAdditionalRequiredSlots(slotIngredient.compression, inv[i].stackSize) > freeSlots) {
				placeChecksFailed++;
				continue;
			}

			compression = slotIngredient.compression;
			slot = i;
		}

		if (slot == -1 && placeChecksFailed == 0)
			return -2;

		return slot;
	}

	private static int getAdditionalRequiredSlots(int compression, int amount) {
		if (compression == 0)
			return 0;

		int items = 9 * amount;
		int stacks = (int) Math.ceil(items / 64d) - 1;
		int rest = items - stacks * 64;

		return stacks + getAdditionalRequiredSlots(compression - 1, rest);
	}

	private static class PredeterminedIngredient extends Ingredient {

		private final int slot;

		public PredeterminedIngredient(ItemStack stack, int slot) {
			super(stack, -1);
			this.slot = slot;
		}

		@Override
		public int getSlot(int[] excludedSlots) {
			return slot;
		}

		@Override
		public boolean equals(Ingredient other) {
			return itemEquals(other);
		}

	}

}
