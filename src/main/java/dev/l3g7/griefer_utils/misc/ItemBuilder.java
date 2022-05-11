package dev.l3g7.griefer_utils.misc;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.List;

public class ItemBuilder {

    private final ItemStack itemStack;

    public ItemBuilder(Item item) {
        itemStack = new ItemStack(item);
    }

    public ItemBuilder(ItemStack item) {
        itemStack = item;
    }

    public ItemBuilder name(String name) {
        itemStack.setStackDisplayName(name);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        NBTTagCompound tag = itemStack.getTagCompound();

        NBTTagList loreTag = new NBTTagList();
        lore.stream().map(NBTTagString::new).forEach(loreTag::appendTag);
        tag.getCompoundTag("display").setTag("Lore", loreTag);

        itemStack.setTagCompound(tag);
        return this;
    }

    public ItemBuilder enchant() {
        return enchant(Enchantment.protection);
    }

    public ItemBuilder enchant(Enchantment enchantment) {
        return enchant(enchantment, 1);
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        itemStack.addEnchantment(enchantment, level);
        return this;
    }

    public ItemStack build() {
        return itemStack;
    }
}
