/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.farms;

import dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.gui.GuiSearchable;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.Comparator;
import java.util.TreeSet;

import static dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.farms.SpawnerType.SPAWNER_TYPES;

public class GuiFilterType extends GuiSearchable<SpawnerType> {

	private final GuiFarms guiFarms;

	public GuiFilterType(GuiFarms guiFarms) {
		super("§8§lFarmen", 7, guiFarms, "Typ suchen...", Comparator.comparing(st -> st.germanName));
		this.guiFarms = guiFarms;
	}

	@Override
	protected int fillEntriesOnEmptySearch(TreeSet<SpawnerType> entries) {
		entries.addAll(SPAWNER_TYPES.values());
		updatePage();
		return entries.size();
	}

	@Override
	protected Iterable<SpawnerType> getAllEntries() {
		return SPAWNER_TYPES.values();
	}

	@Override
	protected void addItem(SpawnerType entry, int slot) {
		Runnable onClick = () -> {
			searchField.setText("");
			GuiFarms.typeFiler = entry;
			guiFarms.open();
		};

		if (entry.isCobblestone()) {
			addItem(slot, ItemUtil.createItem(Blocks.cobblestone, 0, "§6§nBruchstein"), onClick);
			return;
		}

		TextureItem textureItem = new TextureItem(entry.texture, 12, new ItemStack(Blocks.command_block, 1).setStackDisplayName("§6§n" + entry.germanName));
		addTextureItem(slot, textureItem, onClick);
	}

	@Override
	protected boolean matchesSearch(SpawnerType entry, String search) {
		return entry.matchesFilter(search);
	}
}
