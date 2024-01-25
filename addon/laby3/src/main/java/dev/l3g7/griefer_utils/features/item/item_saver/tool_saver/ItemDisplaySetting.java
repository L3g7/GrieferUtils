/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver.tool_saver;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.settings.elements.ListEntrySetting;
import net.minecraft.item.ItemStack;

class ItemDisplaySetting extends ListEntrySetting {

	final String name;
	final ItemStack stack;

	private final IconStorage iconStorage = new IconStorage();

	ItemDisplaySetting(String name, ItemStack stack) {
		super(true, false, false);
		container = FileProvider.getSingleton(ToolSaver.class).enabled;
		name(name);
		this.name = name;
		this.stack = stack;
		icon(stack);
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	protected void onChange() {
		FileProvider.getSingleton(ToolSaver.class).onChange();
	}

}
