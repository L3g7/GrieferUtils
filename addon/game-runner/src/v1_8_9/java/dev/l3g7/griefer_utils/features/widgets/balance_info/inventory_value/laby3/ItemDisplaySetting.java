/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.balance_info.inventory_value.laby3;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Icon;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ItemDisplaySetting extends ListEntrySetting {

	private ItemStack stack; // lazy-loaded
	private final String stackNbt;
	public long value;

	public ItemDisplaySetting(String stackNbt, long value) {
		super(true, false, false, new Icon.WrappedIcon(Icon.of(Blocks.stone)));
		container = FileProvider.getSingleton(InventoryValue.class).rawBooleanElement;
		this.stackNbt = stackNbt;
		this.value = value;
	}

	public ItemDisplaySetting(ItemStack stack, long value) {
		this((String) null, value);

		this.stack = stack;
		this.iconData = new Icon.WrappedIcon(Icon.of(stack));
		setDisplayName(stack.getDisplayName());
	}

	private void initStack() {
		if (stack == null) {
			stack = ItemUtil.fromNBT(stackNbt);
			this.iconData = new Icon.WrappedIcon(Icon.of(stack));
			setDisplayName(stack.getDisplayName());
		}
	}

	@Override
	public int getObjectWidth() {
		return 0;
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

		DrawUtils.drawString(displayName, x + 25, y + 2);
		DrawUtils.drawString("§o➡ " + Constants.DECIMAL_FORMAT_98.format(value) + "$", x + 25, y + 12);
	}

}
