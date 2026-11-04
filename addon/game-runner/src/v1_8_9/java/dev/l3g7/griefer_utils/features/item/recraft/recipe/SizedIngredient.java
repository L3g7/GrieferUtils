/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.recipe;

import dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.SpecificItemSaver;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftAction.Ingredient;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class SizedIngredient {

	private final Ingredient ingredient;
	private final int size;

	static List<SizedIngredient> fromIngredients(Ingredient[] ingredients) {
		if (ingredients == null)
			return null;

		Map<Ingredient, AtomicInteger> ingredientCounts = new HashMap<>();
		for (Ingredient ingredient : ingredients)
			if (ingredient != null)
				ingredientCounts.computeIfAbsent(ingredient, k -> new AtomicInteger(0)).incrementAndGet();

		List<SizedIngredient> sizedIngredients = new ArrayList<>();
		for (Map.Entry<Ingredient, AtomicInteger> entry : ingredientCounts.entrySet())
			sizedIngredients.add(new SizedIngredient(entry.getKey(), entry.getValue().intValue()));

		return sizedIngredients;
	}

	SizedIngredient(Ingredient ingredient, int size) {
		this.ingredient = ingredient;
		this.size = size;
	}

	long toLong() {
		return ingredient.toLong() + (long) size * 10000_0000_0000L;
	}

	static SizedIngredient fromLong(long value) {
		Ingredient ingredient = Ingredient.fromLong(value);
		int size = (int) (value / 10000_0000_0000L % 10000);
		return new SizedIngredient(ingredient, size);
	}

	boolean isAvailable(ItemStack[] stacks, boolean checkForItemSaver) {
		int count = 0;

		for (ItemStack stack : stacks) {
			if (!ingredient.equals(Ingredient.fromItemStack(stack)))
				continue;

			if (checkForItemSaver && SpecificItemSaver.getProtectionFor(stack).isProtected())
				return false;

			count += stack.stackSize;
			if (count >= size)
				return true;
		}

		return false;
	}

	public String toString() {
		return size + "x " + ingredient.toString();
	}

}
