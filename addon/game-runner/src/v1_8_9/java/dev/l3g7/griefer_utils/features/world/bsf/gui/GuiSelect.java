/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.bsf.gui;

import dev.l3g7.griefer_utils.core.misc.gui.guis.GuiBigChest;
import dev.l3g7.griefer_utils.features.world.bsf.data.BSFSearchable;
import dev.l3g7.griefer_utils.features.world.bsf.data.Category;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

class GuiSelect extends GuiBigChest {

	public GuiSelect(String title, Category category, GuiBigChest previousGui) {
		super(title + (category.getIcon() == null ? "" : " | " + category.getName().singular), 2 + (int) Math.ceil(category.entries.length / 7d), previousGui);
		int counter = 10;

		if (category.getIcon() != null) { // ALL categories
			ItemStack stack = category.getIcon().copy().setStackDisplayName("§fGesamte " + category.getName().singular + "-Kategorie ");
			addItem(counter, stack, () -> {
				new GuiSearches(category, title + " | " + category.getName().singular + "-Kategorie", category.getIcon(), this).open();
			});
			counter += 2;
		}

		for (BSFSearchable entry : category.entries) {
			addItem(counter, entry.getIcon(), () -> {
				if (entry instanceof Category c) {
					mc().displayGuiScreen(new GuiSelect(title, c, this));
					return;
				}

				new GuiSearches(entry, title + " | " + entry.getName().singular, entry.getIcon(), this).open();
			});

			if (counter++ % 9 == 7)
				counter += 2;
		}
	}

}
