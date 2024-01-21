/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftAction;
import dev.l3g7.griefer_utils.features.item.recraft.recipe.RecipeAction;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

public abstract class RecraftAction {

	protected abstract JsonElement toJson();

	static RecraftAction loadFromJson(JsonElement element, boolean craft) {
		if (craft)
			return CraftAction.fromJson(element);
		return RecipeAction.fromJson(element);
	}

	public static class Ingredient {

		final int itemId;
		final int compression;
		final int meta;
		private int lastSlotIndex = -1;

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

		public Ingredient(int itemId, int meta, int compression) {
			this.itemId = itemId;
			this.meta = meta;
			this.compression = compression;
		}

		public static boolean check(Ingredient ingredient, ItemStack stack) {
			if (ingredient == null)
				return true;

			return ingredient.equals(Ingredient.fromItemStack(stack));
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

		public int getSlot(int[] excludedSlots) {
			ItemStack[] inv = player().inventory.mainInventory;
			invLoop:
			for (int i = 0; i < inv.length; i++) {
				if (!check(this, inv[i]))
					continue;

				for (int excludedSlot : excludedSlots)
					if (i == excludedSlot)
						continue invLoop;

				return i;
			}

			return -1;
		}

		public int getSlot() {
			ItemStack[] inv = player().inventory.mainInventory;
			for (int i = 0; i < inv.length; i++) {
				if (!this.equals(fromItemStack(inv[i])))
					continue;

				if (i < 9)
					i += 36;
				return lastSlotIndex = i + 45;
			}

			// Check if the item still is in the player's cursor
			if (this.equals(fromItemStack(player().inventory.getItemStack())))
				return lastSlotIndex;

			return -1;
		}

		public long toLong() {
			long result = compression;
			// 10000 is used instead of real bit shifting to maintain readability
			result += (long) meta * 10000;
			result += (long) itemId * 10000_0000;
			return result;
		}

		public static Ingredient fromLong(long value) {
			int compression = (int) (value % 10000);
			int meta = (int) (value / 10000 % 10000);
			int itemId = (int) (value / 10000_0000 % 10000);
			return new Ingredient(itemId, meta, compression);
		}

		public String toString() {
			return String.format("%d:%d (%d)", itemId, meta, compression);
		}

	}

}
