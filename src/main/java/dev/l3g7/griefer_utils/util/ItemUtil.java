package dev.l3g7.griefer_utils.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;

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

	public static boolean hasLore(ItemStack itemStack) {
		return getLore(itemStack).size() > 0;
	}

	public static String getLastLore(ItemStack itemStack) {
		List<String> lore = getLore(itemStack);
		return lore.size() > 0 ? lore.get(lore.size() - 1) : "";
	}
}
