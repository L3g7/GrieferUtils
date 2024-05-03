/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.laby4.crafter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.laby4.RecraftAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CraftAction extends RecraftAction {

	public final Ingredient[] ingredients;

	public CraftAction(Ingredient[] ingredients) {
		this.ingredients = ingredients;
	}

	public int[] getSlotsFromHotbar() {
		int[] slots = new int[9];
		Arrays.fill(slots, -1);

		for (int i = 0; i < ingredients.length; i++) {
			if (ingredients[i] == null) {
				slots[i] = -1;
				continue;
			}

			int slot = slots[i] = ingredients[i].getSlot(slots);
			if (slot == -1 || slot >= 9)
				return null;
		}

		return slots;
	}

	public int[] getSlotsFromInventory() {
		int[] slots = new int[9];
		Arrays.fill(slots, -1);

		for (int i = 0; i < ingredients.length; i++) {
			if (ingredients[i] == null) {
				slots[i] = -1;
				continue;
			}

			if ((slots[i] = ingredients[i].getSlot(slots)) == -1)
				return null;

		}

		return slots;
	}

	public JsonElement toJson() {
		JsonArray array = new JsonArray();
		for (Ingredient ingredient : ingredients) {
			if (ingredient == null)
				array.add(JsonNull.INSTANCE);
			else
				array.add(new JsonPrimitive(ingredient.toLong()));
		}

		return array;
	}

	public static CraftAction fromJson(JsonElement element) {
		JsonArray array = element.getAsJsonArray();
		List<Ingredient> ingredients = new ArrayList<>();
		for (JsonElement e : array)
			ingredients.add(e.isJsonNull() ? null : Ingredient.fromLong(e.getAsLong()));

		return new CraftAction(ingredients.toArray(new Ingredient[0]));
	}

	@Override
	public String toString() {
		return Arrays.stream(ingredients).map(Ingredient::toString).collect(Collectors.joining("\n"));
	}

}
