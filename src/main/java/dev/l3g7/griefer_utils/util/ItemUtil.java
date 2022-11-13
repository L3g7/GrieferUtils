package dev.l3g7.griefer_utils.util;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

	public static ItemStack MISSING_TEXTURE = new ItemStack(Blocks.stone, 1, 10000);

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

	public static boolean canBeRepaired(ItemStack itemStack) {
		// The repair cost of the item (Source: ContainerRepair.updateRepairOutput())
		// If the item is only damaged 1/4, you can repair it with a single material of the same type (i.e. a diamond), thus costing only 1 level
		// more than the repair value. Otherwise, it can be repaired with another item of the same type (i.e. a diamond sword), costing 2 levels more.
		int xpCost = itemStack.getRepairCost() + (itemStack.getItemDamage() >= itemStack.getMaxDamage() / 4 ? 1 : 2);

		return xpCost < 40;
	}
}
