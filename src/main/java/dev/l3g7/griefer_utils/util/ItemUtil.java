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

package dev.l3g7.griefer_utils.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for item processing.
 */
public class ItemUtil {

	public static List<String> getLore(ItemStack itemStack) {
		List<String> lore = new ArrayList<>();

		if (itemStack == null)
			return lore;

		NBTTagCompound tag = itemStack.getTagCompound();
		if (tag == null || !tag.hasKey("display", 10))
			return lore;

		NBTTagCompound nbt = tag.getCompoundTag("display");
		if (nbt.getTagId("Lore") != 9)
			return lore;

		NBTTagList tagList = nbt.getTagList("Lore", 8);
		if (tagList.tagCount() < 1)
			return lore;

		for (int i = 0; i < tagList.tagCount(); i++)
			lore.add(tagList.getStringTagAt(i));

		return lore;
	}

	public static String getLastLore(ItemStack itemStack) {
		List<String> lore = getLore(itemStack);
		return lore.size() > 0 ? lore.get(lore.size() - 1) : "";
	}

	public static boolean canBeRepaired(ItemStack itemStack) {
		// The repair cost of the item (Source: ContainerRepair.updateRepairOutput())
		// If the item is only damaged 1/4, you can repair it with a single material of the same type (i.e. a diamond), thus costing only 1 level
		// more than the repair value. Otherwise, it can be repaired with another item of the same type (i.e. a diamond sword), costing 2 levels more.
		int xpCost = itemStack.getRepairCost() + (itemStack.getItemDamage() >= itemStack.getMaxDamage() / 4 ? 1 : 2);

		return xpCost < 40;
	}

	public static ItemStack createItem(Item item, int meta, String name) { return createItem(new ItemStack(item, 1, meta), false, name); }
	public static ItemStack createItem(Item item, int meta, boolean enchanted) { return createItem(new ItemStack(item, 1, meta), enchanted, null); }
	public static ItemStack createItem(Block block, int meta, boolean enchanted) { return createItem(new ItemStack(block, 1, meta), enchanted, null); }
	public static ItemStack createItem(Block block, int meta, String name) { return createItem(new ItemStack(block, 1, meta), false, name); }
	public static ItemStack createItem(ItemStack stack, boolean enchanted, String name) {
		if (enchanted) {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setTag("ench", new NBTTagList());
		}

		if (name != null)
			stack.setStackDisplayName(name);

		return stack;
	}

}
