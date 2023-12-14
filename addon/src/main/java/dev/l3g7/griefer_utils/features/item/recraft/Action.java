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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

class Action {

	private final int slot;
	private final Ingredient ingredient;
	private final List<SizedIngredient> craftingIngredients;

	public Action(int slot, List<SizedIngredient> craftingIngredients) {
		this.slot = slot;
		this.craftingIngredients = craftingIngredients;
		this.ingredient = null;
	}

	public Action(Ingredient ingredient) {
		this.ingredient = ingredient;
		this.slot = -1;
		this.craftingIngredients = null;
	}

	/**
	 * @return
	 * true: if this action was successfull<br>
	 * false: if this action failed<br>
	 * null: if this action was skipped
	 */
	public Boolean execute(GuiChest chest) {
		if (ingredient != null) {
			int ingredientSlot = ingredient.getSlot();
			if (ingredientSlot == -1) {
				displayAchievement("§c§lFehler \u26A0", "Ein benötigtes Item ist nicht verfügbar!");
				return false;
			}

			mc().playerController.windowClick(chest.inventorySlots.windowId, ingredientSlot, 0, 0, player());
			return true;
		}

		if (craftingIngredients != null) {
			for (SizedIngredient craftingIngredient : craftingIngredients) {
				if (!craftingIngredient.isAvailable()) {
					displayAchievement("§eAktion übersprungen \u26A0", "Du hattest nicht genügend Zutaten im Inventar!");
					return null;
				}
			}
		}

		mc().playerController.windowClick(chest.inventorySlots.windowId, Math.abs(slot), 0, slot < 0 ? 1 : 0, player());
		return true;
	}

	JsonElement toJson() {
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

	static Action fromJson(JsonElement element) {
		if (element.isJsonPrimitive()) {
			long value = element.getAsLong();
			if (value < 0xFFFF)
				return new Action((int) value, null);

			return new Action(Ingredient.fromLong(value));
		}

		JsonObject object = element.getAsJsonObject();
		if (!object.has("crafting_ingredients")) // Ensure backwards compatibility
			return new Action(new Ingredient(object.get("id").getAsInt(), object.get("meta").getAsInt(), object.get("compression").getAsInt()));

		int slot = object.get("slot").getAsInt();

		JsonArray craftingJson = object.getAsJsonArray("crafting_ingredients");
		if (craftingJson == null)
			return new Action(slot, null);

		List<SizedIngredient> ingredients = new ArrayList<>();
		for (JsonElement jsonElement : craftingJson)
			ingredients.add(SizedIngredient.fromLong(jsonElement.getAsLong()));

		return new Action(slot, ingredients);
	}

	@Override
	public String toString() {
		if (ingredient != null)
			return ingredient.toString();

		return slot + " " + craftingIngredients;
	}

	static class Ingredient {

		final int itemId;
		final int compression;
		final int meta;

		public static Ingredient fromItemStack(ItemStack stack) {
			if (stack == null || stack.isItemStackDamageable())
				return null;

			if (!EnchantmentHelper.getEnchantments(stack).isEmpty())
				return null;

			// Check if the item is compressed
			int decompressedAmount = ItemUtil.getDecompressedAmount(stack);
			if (decompressedAmount == stack.stackSize)
				return new Ingredient(stack, 0);

			// Item is compressed, make sure it hasn't been placed yet
			int compressionLevel = ItemUtil.getCompressionLevel(stack);
			if (Math.pow(9, compressionLevel) * stack.stackSize != decompressedAmount)
				return null;

			return new Ingredient(stack, compressionLevel);
		}

		Ingredient(ItemStack stack, int compression) {
			this(Item.getIdFromItem(stack.getItem()), stack.getMetadata(), compression);
		}

		private Ingredient(int itemId, int meta, int compression) {
			this.itemId = itemId;
			this.meta = meta;
			this.compression = compression;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Ingredient))
				return false;

			return equals((Ingredient) obj);
		}

		boolean equals(Ingredient other) {
			if (other == null)
				return false;

			return meta == other.meta
				&& itemId == other.itemId
				&& compression == other.compression;
		}

		/**
		 * @return A slot index containing an item corresponding to this ingredient
		 */
		private int getSlot() {
			ItemStack[] inv = player().inventory.mainInventory;
			for (int i = 0; i < inv.length; i++) {
				if (!this.equals(fromItemStack(inv[i])))
					continue;

				if (i < 9)
					i += 36;
				return i + 45;
			}

			return -1;
		}

		long toLong() {
			long result = compression;
			// 10000 is used instead of real bit shifting to maintain readability
			result += (long) meta * 10000;
			result += (long) itemId * 10000_0000;
			return result;
		}

		static Ingredient fromLong(long value) {
			int compression = (int) (value % 10000);
			int meta = (int) (value / 10000 % 10000);
			int itemId = (int) (value / 10000_0000 % 10000);
			return new Ingredient(itemId, meta, compression);
		}

		public String toString() {
			return String.format("%d:%d (%d)", itemId, meta, compression);
		}

		@Override
		public int hashCode() {
			return itemId + meta * 10000;
		}

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
