/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftAction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

public class RecipeAction extends RecraftAction {

	private final int slot;
	private final Ingredient ingredient;
	private final List<SizedIngredient> craftingIngredients;

	public RecipeAction(int slot, List<SizedIngredient> craftingIngredients) {
		this.slot = slot;
		this.craftingIngredients = craftingIngredients;
		this.ingredient = null;
	}

	public RecipeAction(Ingredient ingredient) {
		this.ingredient = ingredient;
		this.slot = -1;
		this.craftingIngredients = null;
	}

	/**
	 * @return
	 * true: if this action was successful<br>
	 * false: if this action failed<br>
	 * null: if this action was skipped
	 */
	public Boolean execute(int windowId, boolean hasSucceeded) {
		if (ingredient != null) {
			int ingredientSlot = ingredient.getSlot();
			if (ingredientSlot == -1) {
				if (hasSucceeded)
					return null;

				labyBridge.notify("§c§lFehler \u26A0", "Ein benötigtes Item ist nicht verfügbar!");
				return false;
			}

			click(windowId, ingredientSlot);
			return true;
		}

		if (craftingIngredients != null) {
			for (SizedIngredient craftingIngredient : craftingIngredients) {
				if (!craftingIngredient.isAvailable()) {
					if (!hasSucceeded)
						labyBridge.notify("§eAktion übersprungen \u26A0", "Du hattest nicht genügend Zutaten im Inventar!");

					return null;
				}
			}
		}

		click(windowId, slot);
		return true;
	}

	private static void click(int windowId, int slot) {
		mc().getNetHandler().addToSendQueue(new C0EPacketClickWindow(windowId, Math.abs(slot), 0, slot < 0 ? 1 : 0, null, (short) 0));
	}

	public JsonElement toJson() {
		if (ingredient != null)
			return new JsonPrimitive(ingredient.toLong());

		if (craftingIngredients == null)
			return new JsonPrimitive(slot);

		JsonObject object = new JsonObject();
		object.addProperty("slot", slot);
		JsonArray craftingJson = new JsonArray();

		for (SizedIngredient craftingIngredient : craftingIngredients)
			craftingJson.add(new JsonPrimitive(craftingIngredient.toLong()));

		object.add("crafting_ingredients", craftingJson);
		return object;
	}

	public static RecipeAction fromJson(JsonElement element) {
		if (element.isJsonPrimitive()) {
			long value = element.getAsLong();
			if (value < 0xFFFF)
				return new RecipeAction((int) value, null);

			return new RecipeAction(Ingredient.fromLong(value));
		}

		JsonObject object = element.getAsJsonObject();
		if (!object.has("crafting_ingredients")) // Ensure backwards compatibility
			return new RecipeAction(new Ingredient(object.get("id").getAsInt(), object.get("meta").getAsInt(), object.get("compression").getAsInt()));

		int slot = object.get("slot").getAsInt();

		JsonArray craftingJson = object.getAsJsonArray("crafting_ingredients");
		if (craftingJson == null)
			return new RecipeAction(slot, null);

		List<SizedIngredient> ingredients = new ArrayList<>();
		for (JsonElement jsonElement : craftingJson)
			ingredients.add(SizedIngredient.fromLong(jsonElement.getAsLong()));

		return new RecipeAction(slot, ingredients);
	}

	@Override
	public String toString() {
		if (ingredient != null)
			return ingredient.toString();

		return slot + " " + craftingIngredients;
	}

	static class SizedIngredient {

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

		boolean isAvailable() {
			int count = 0;

			for (ItemStack stack : player().inventory.mainInventory) {
				if (!ingredient.equals(Ingredient.fromItemStack(stack)))
					continue;

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

}
