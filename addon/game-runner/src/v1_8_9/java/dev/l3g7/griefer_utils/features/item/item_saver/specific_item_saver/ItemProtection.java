/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.ListEntry;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;
import java.util.List;

public class ItemProtection implements ListEntry<ItemProtection> {

	protected static final ItemProtection UNPROTECTED = new ItemProtection();

	private transient ItemStack stack;

	private final StringSetting name = StringSetting.create()
		.name("Anzeigename")
		.description("Der Anzeigename des Eintrags. Hat keinen Einfluss auf die geschützten Items.")
		.icon("name_tag");

	private final SwitchSetting denyDrop = SwitchSetting.create()
		.name("Droppen unterbinden")
		.description("Ob das Droppen dieses Items unterbunden werden soll.")
		.icon("hopper");

	private final SwitchSetting denyItemPickup = SwitchSetting.create()
		.name("Droppen unterbinden (extrem)")
		.description("Ob das Aufnehmen dieses Items in den Maus-Cursor unterbunden werden soll.")
		.icon("shield_with_sword");

	private final SwitchSetting denyLeftClick = SwitchSetting.create()
		.name("Linksklicks unterbinden")
		.description("Ob Linksklicks mit diesem Item unterbunden werden soll.")
		.icon("weakness");

	private final SwitchSetting denyRightClick = SwitchSetting.create()
		.name("Rechtsklicks unterbinden")
		.description("Ob Rechtsklicks mit diesem Item unterbunden werden soll.")
		.icon("creeper_spawn_egg");

	{
		// if itemPickup is denied, drop must also be denied
		denyItemPickup.callback(v -> {if (v) denyDrop.set(true);});
		denyDrop.callback(v -> {if (!v) denyItemPickup.set(false);});
	}

	public ItemProtection() {}

	public ItemProtection(ItemStack stack) {
		loadFromStack(stack);
	}

	public boolean isProtected() {
		return this != UNPROTECTED;
	}

	public boolean deniesDrop() {
		return denyDrop.get();
	}

	public boolean deniesItemPickup() {
		return denyItemPickup.get();
	}

	public boolean deniesLeftClick() {
		return denyLeftClick.get();
	}

	public boolean deniesRightClick() {
		return denyRightClick.get();
	}

	public boolean appliesTo(ItemStack stack) {
		if (stack == null)
			return false;

		// Compare item
		if (this.stack.getItem() != stack.getItem())
			return false;

		// Compare metadata
		if (!stack.isItemStackDamageable() && this.stack.getMetadata() != stack.getMetadata())
			return false;

		// Compare NBT tags
		NBTTagCompound stackNBT = stack.getTagCompound();
		NBTTagCompound settingNBT = this.stack.getTagCompound();

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

	@Override
	public ItemProtection createNew() {
		return new ItemProtection();
	}

	@Override
	public List<BaseSetting<?>> toSettings() {
		return Arrays.asList(name, denyDrop, denyItemPickup, denyLeftClick, denyRightClick);
	}

	private void loadFromStack(ItemStack stack) {
		this.stack = stack.copy();

		if (this.stack.isItemStackDamageable())
			this.stack.setItemDamage(0);

		if (this.stack.hasTagCompound()) {
			this.stack.getTagCompound().removeTag("display");
			this.stack.getTagCompound().removeTag("RepairCost");
		}

		this.stack.stackSize = 1;

		this.name.set(stack.getDisplayName());

		// Deny left click if item is a tool, otherwise deny right click by default
		denyLeftClick.set(stack.isItemStackDamageable());
		denyRightClick.set(stack.isItemStackDamageable());
	}

	@Override
	public void load(JsonElement element) {
		JsonObject data = element.getAsJsonObject();

		loadFromStack(ItemUtil.fromNBT(data.get("nbt").getAsString()));
		this.name.set(data.get("name").getAsString());

		this.denyDrop.set(data.get("deny_drop").getAsBoolean());
		this.denyItemPickup.set(data.get("deny_item_pickup").getAsBoolean());
		this.denyLeftClick.set(data.get("deny_left_click").getAsBoolean());
		this.denyRightClick.set(data.get("deny_right_click").getAsBoolean());
	}

	@Override
	public JsonElement encode() {
		JsonObject entry = new JsonObject();

		entry.addProperty("nbt", ItemUtil.serializeNBT(this.stack));
		entry.addProperty("name", this.name.get());

		entry.addProperty("deny_drop", this.denyDrop.get());
		entry.addProperty("deny_item_pickup", this.denyItemPickup.get());
		entry.addProperty("deny_left_click", this.denyLeftClick.get());
		entry.addProperty("deny_right_click", this.denyRightClick.get());

		return entry;
	}

	@Override
	public String getName() {
		return this.name.get();
	}

	@Override
	public ItemStack itemIcon() {
		return this.stack;
	}

}
