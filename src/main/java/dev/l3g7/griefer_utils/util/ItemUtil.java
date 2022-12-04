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
