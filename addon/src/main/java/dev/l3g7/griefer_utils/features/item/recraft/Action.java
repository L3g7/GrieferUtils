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

import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

class Action {

	private final int slot;
	private final Ingredient ingredient;

	public Action(int slot, Ingredient ingredient) {
		this.slot = slot;
		this.ingredient = ingredient;
	}

	public void execute(GuiChest chest) {
		if (slot < 54) {
			mc().playerController.windowClick(chest.inventorySlots.windowId, slot, 0, 0, player());
			return;
		}

		int ingredientSlot = ingredient.getSlot();
		if (ingredientSlot == -1)
			return;

		mc().playerController.windowClick(chest.inventorySlots.windowId, ingredientSlot, 0, 0, player());
	}

	void write(PacketBuffer out) {
		out.writeByte((byte) slot);
		if (ingredient != null)
			ingredient.write(out);
	}

	static Action readObject(PacketBuffer in) {
		byte slot = in.readByte();
		return new Action(slot, slot > 53 ? Ingredient.read(in) : null);
	}

	static class Ingredient {

		private final int itemId;
		private final int compression;
		private final int meta;

		public static Ingredient getIngredient(ItemStack stack) {
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
			this(Item.getIdFromItem(stack.getItem()), compression, stack.getMetadata());
		}

		private Ingredient(int itemId, int compression, int meta) {
			this.itemId = itemId;
			this.compression = compression;
			this.meta = meta;
		}

		private boolean equals(Ingredient other) {
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
				if (!this.equals(getIngredient(inv[i])))
					continue;

				if (i < 9)
					i += 36;
				return i + 45;
			}

			return -1;
		}

		void write(PacketBuffer out) {
			out.writeVarIntToBuffer(itemId);
			out.writeByte(compression);
			out.writeByte(meta);
		}

		static Ingredient read(PacketBuffer in) {
			return new Ingredient(in.readVarIntFromBuffer(), in.readByte(), in.readByte());
		}

	}

}
