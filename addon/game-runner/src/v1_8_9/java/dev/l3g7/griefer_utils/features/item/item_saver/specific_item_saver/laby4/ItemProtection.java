/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.laby4;

import dev.l3g7.griefer_utils.core.api.misc.functions.Function;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import static dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.laby4.ItemProtection.ProtectionType.LEFT_CLICK;
import static dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.laby4.ItemProtection.ProtectionType.RIGHT_CLICK;

public class ItemProtection {

	static final ItemProtection BONZE = new ItemProtection(ItemUtil.fromNBT("{id:\"minecraft:diamond_sword\",Count:1b,tag:{ench:[0:{lvl:21s,id:16s},1:{lvl:3s,id:34s},2:{lvl:2s,id:20s},3:{lvl:5s,id:61s},4:{lvl:21s,id:21s}],display:{Name:\"§6Klinge von GrafBonze\"}},Damage:0s}"));
	static final ItemProtection BONZE_24 = new ItemProtection(ItemUtil.fromNBT("{id:\"minecraft:diamond_sword\",Count:1b,tag:{ench:[0:{lvl:24s,id:16s},1:{lvl:3s,id:34s},2:{lvl:2s,id:20s},3:{lvl:5s,id:61s},4:{lvl:24s,id:21s}],display:{Name:\"§6Klinge von GrafBonze\"}},Damage:0s}"));
	static final ItemProtection BIRTH = new ItemProtection(ItemUtil.fromNBT("{id:\"minecraft:diamond_sword\",Count:1b,tag:{ench:[0:{lvl:21s,id:16s},1:{lvl:2s,id:20s},2:{lvl:5s,id:61s},3:{lvl:21s,id:21s}],display:{Name:\"§4B§aI§3R§2T§eH §4§lKlinge\"}},Damage:0s}"));

	static final ItemProtection UNPROTECTED = new ItemProtection();

	ItemStack stack;
	String name;
	final boolean[] states = new boolean[ProtectionType.values().length];

	private ItemProtection() {}

	ItemProtection(ItemStack stack) {
		this.stack = stack.copy();

		if (this.stack.isItemStackDamageable())
			this.stack.setItemDamage(0);

		if (this.stack.hasTagCompound()) {
			this.stack.getTagCompound().removeTag("display");
			this.stack.getTagCompound().removeTag("RepairCost");
		}

		this.stack.stackSize = 1;

		this.name = stack.getDisplayName();
		states[LEFT_CLICK.ordinal()] = stack.isItemStackDamageable();
		states[RIGHT_CLICK.ordinal()] = !stack.isItemStackDamageable();
	}

	public boolean isProtected() {
		return this != UNPROTECTED;
	}

	public boolean isProtectedAgainst(ProtectionType type) {
		return states[type.ordinal()];
	}

	public boolean appliesTo(ItemStack stack) {
		if (stack == null)
			return false;

		ItemProtection protection = this;
		ItemStack protectedStack = protection.stack;

		// Compare item
		if (protectedStack.getItem() != stack.getItem())
			return false;

		// Compare metadata
		if (!stack.isItemStackDamageable() && protectedStack.getMetadata() != stack.getMetadata())
			return false;

		// Compare NBT tags
		NBTTagCompound stackNBT = stack.getTagCompound();
		NBTTagCompound settingNBT = protectedStack.getTagCompound();

		if (stackNBT == null)
			return settingNBT == null;

		stackNBT = ItemUtil.safeCopy(stackNBT);
		NBTTagCompound cleanedStackNBT = new NBTTagCompound(); // A copy of stackNBT without display, RepairCost
		for (String s : stackNBT.getKeySet()) {
			if (s.equals("display") || s.equals("RepairCost"))
				continue;

			NBTBase tag = stackNBT.getTag(s);
			cleanedStackNBT.setTag(s, tag == null ? null : tag.copy());
		}

		return cleanedStackNBT.equals(settingNBT);
	}

	public enum ProtectionType {

		DROP("drop", "Droppen unterbinden", "Ob das Droppen dieses Items unterbunden werden soll.", Blocks.dropper),
		ITEM_PICKUP("extreme_drop", "Droppen unterbinden (extrem)", "Ob das Aufnehmen dieses Items in den Maus-Cursor unterbunden werden soll.", "shield_with_sword"),
		LEFT_CLICK("leftclick", "Linksklicks unterbinden", "Ob Linksklicks mit diesem Item unterbunden werden soll.", Items.diamond_sword),
		RIGHT_CLICK("rightclick", "Rechtsklicks unterbinden", "Ob Rechtsklicks mit diesem Item unterbunden werden soll.", Items.bow);

		private final Function<ItemProtection, SwitchSetting> settingSupplier;
		final String configKey;

		ProtectionType(String configKey, String name, String description, Object icon) {
			this.configKey = configKey;
			settingSupplier = protection -> SwitchSetting.create()
				.name(name)
				.description(description)
				.icon(icon)
				.defaultValue(protection.states[ordinal()])
				.callback(s -> protection.states[ordinal()] = s);
		}

		SwitchSetting createSetting(ItemProtection protection) {
			return settingSupplier.apply(protection);
		}

	}

}
