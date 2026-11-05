/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver.tool_saver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.ListEntry;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

public final class ToolExclusion implements ListEntry {

	private transient ItemStack stack;
	private String name;

	public ToolExclusion() {}

	public ToolExclusion(ItemStack stack) {
		this.stack = normalizeStack(stack);
		this.name = stack.getDisplayName();
	}

	public static ItemStack normalizeStack(ItemStack stack) {
		ItemStack is = stack.copy();
		is.stackSize = 1;

		if (is.isItemStackDamageable())
			is.setItemDamage(0);

		if (is.hasTagCompound()) {
			is.getTagCompound().removeTag("display");
			is.getTagCompound().removeTag("RepairCost");
			if (is.getTagCompound().hasNoTags())
				is.setTagCompound(null);
		}

		return is;
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	public List<BaseSetting<?>> toSettings() {
		return Collections.emptyList();
	}

	@Override
	public void load(JsonElement element) {
		JsonObject data = element.getAsJsonObject();
		this.stack = ItemUtil.fromNBT(data.get("nbt").getAsString());
		this.name = data.get("name").getAsString();
	}

	@Override
	public JsonElement encode() {
		JsonObject data = new JsonObject();
		data.addProperty("nbt", ItemUtil.serializeNBT(this.stack));
		data.addProperty("name", this.name);
		return data;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public ItemStack itemIcon() {
		return this.stack;
	}
}
