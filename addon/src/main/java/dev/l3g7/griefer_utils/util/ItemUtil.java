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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.init.Blocks.*;

/**
 * A utility class for item processing.
 */
public class ItemUtil {

	public static final List<ItemStack> ALL_ITEMS = new ArrayList<>();
	public static final List<ItemStack> CB_ITEMS = new ArrayList<>();

	public static ItemStack fromNBT(String nbt) {
		try {
			return ItemStack.loadItemStackFromNBT(JsonToNBT.getTagFromJson(nbt));
		} catch (NBTException e) {
			e.printStackTrace();
			return createItem(barrier, 0, "§4Fehler");
		}
	}

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

	public static ItemStack setLore(ItemStack itemStack, String... lore) {
		return setLore(itemStack, Arrays.asList(lore));
	}

	public static ItemStack setLore(ItemStack itemStack, List<String> lore) {
		NBTTagCompound tag = itemStack.getTagCompound();
		if (tag == null)
			tag = new NBTTagCompound();

		NBTTagCompound display = tag.getCompoundTag("display");
		NBTTagList loreTag = new NBTTagList();

		for (String s : lore)
			loreTag.appendTag(new NBTTagString(s));

		display.setTag("Lore", loreTag);
		tag.setTag("display", display);
		itemStack.setTagCompound(tag);
		return itemStack;
	}

	public static String serializeNBT(ItemStack stack) {
		return stack.writeToNBT(new NBTTagCompound()).toString();
	}

	public static boolean canBeRepaired(ItemStack itemStack) {
		// The repair cost of the item (Source: ContainerRepair#updateRepairOutput()).
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

	static {
		for (Item item : Item.itemRegistry) {
			if (item == null
				|| item == Item.getItemFromBlock(Blocks.farmland) // Has no model
				|| item == Item.getItemFromBlock(Blocks.lit_furnace)) // Has no model
				continue;

			item.getSubItems(item, CreativeTabs.tabAllSearch, ALL_ITEMS);
		}

		ALL_ITEMS.add(new ItemStack(Items.potionitem));
		ALL_ITEMS.sort(Comparator.comparing(ItemStack::getDisplayName));

		CB_ITEMS.add(createItem(Items.nether_star, 0, "Egal"));

		Block[] blocks = new Block[] {diamond_block, emerald_block, gold_block, redstone_block, lapis_block, coal_block, emerald_ore, redstone_ore, diamond_ore, gold_ore, iron_ore, coal_ore, lapis_ore, bedrock, gravel, obsidian, barrier, iron_block, barrier, prismarine, mossy_cobblestone, brick_block};
		for (int i = 0; i < blocks.length; i++)
			CB_ITEMS.add(createItem(blocks[i], 0, "CB" + (i + 1)));

		CB_ITEMS.set(17, createItem(stone, 6, "CB17"));
		CB_ITEMS.set(19, createItem(prismarine, 2, "CB19"));
		CB_ITEMS.add(createItem(sapling, 5, "Nature"));
		CB_ITEMS.add(createItem(sapling, 3, "Extreme"));
		CB_ITEMS.add(createItem(netherrack, 0, "CBE"));
		CB_ITEMS.add(createItem(Items.water_bucket, 0, "Wasser"));
		CB_ITEMS.add(createItem(Items.lava_bucket, 0, "Lava"));
		CB_ITEMS.add(createItem(beacon, 0, "Event"));
	}

}
