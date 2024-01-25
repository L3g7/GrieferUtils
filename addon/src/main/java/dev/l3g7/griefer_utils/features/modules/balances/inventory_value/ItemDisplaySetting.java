/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules.balances.inventory_value;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.settings.elements.ListEntrySetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class ItemDisplaySetting extends ListEntrySetting {

	private ItemStack stack; // lazy-loaded
	private final String stackNbt;
	public long value;

	private final IconStorage iconStorage = new IconStorage();

	public ItemDisplaySetting(String stackNbt, long value) {
		super(true, false, false);
		container = FileProvider.getSingleton(InventoryValue.class).rawBooleanElement;
		this.stackNbt = stackNbt;
		this.value = value;
	}

	public ItemDisplaySetting(ItemStack stack, long value) {
		this((String) null, value);

		this.stack = stack;
		icon(stack);
		name(stack.getDisplayName());
	}

	private void initStack() {
		if (stack == null) {
			stack = ItemUtil.fromNBT(stackNbt);
			icon(stack);
			name(stack.getDisplayName());
		}
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	@Override
	protected void onChange() {
		InventoryValue.onChange();
	}

	public ItemStack getStack() {
		initStack();
		return stack;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		initStack();

		String displayName = getDisplayName();
		setDisplayName("§f");
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		setDisplayName(displayName);

		drawUtils().drawString(displayName, x + 25, y + 2);
		drawUtils().drawString("§o➡ " + Constants.DECIMAL_FORMAT_98.format(value) + "$", x + 25, y + 12);
	}

}
