/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.bsf.data;

import dev.l3g7.griefer_utils.core.misc.gui.guis.GuiBigChest;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public enum Category implements BSFSearchable {

	PLAINS(new BSFName("", "Ebene", "n"), new Color(0x8DB360), Biome.PLAINS, Biome.SUNFLOWER_PLAINS, Biome.SNOWY_TUNDRA),
	BIRCH(new BSFName("n", "Birkenwald", "Birkenwälder"), new Color(0x589C6C), Biome.BIRCH_FOREST, Biome.TALL_BIRCH_FOREST),
	FOREST(new BSFName("n", "Wald", "Wälder"), new Color(0x056621), Biome.FOREST, Biome.DARK_FOREST, BIRCH, Biome.FLOWER_FOREST),
	MOUNTAINS(new BSFName("", "Berge", ""), new Color(0x606060), Biome.MOUNTAINS, Biome.GRAVELLY_MOUNTAINS, Biome.SNOWY_MOUNTAINS),
	TAIGA(new BSFName("", "Taiga", "s"), new Color(0x0B6659), Biome.TAIGA, Biome.GIANT_TAIGA, Biome.SNOWY_TAIGA),
	BEACH(new BSFName("n", "Strand", "Strände"), new Color(0xFADE55), Biome.BEACH, Biome.STONE_SHORE, Biome.SNOWY_BEACH),
	ALL_BIOMES(null, null,
		Biome.OCEAN, Biome.ICE_SPIKES, Category.BEACH, Biome.DESERT, Biome.BADLANDS, Category.MOUNTAINS, Biome.MUSHROOM_FIELDS,
		Category.PLAINS, Category.FOREST, Category.TAIGA, Biome.JUNGLE, Biome.SAVANNA, Biome.SWAMP
	),
	ALL_STRUCTURES(null, null, Structure.values());

	public final BSFSearchable[] entries;
	public final List<Integer> biomeIds = new ArrayList<>();
	private final BSFName name;
	private final GuiBigChest.TextureItem icon;
	private final Color color;

	Category(BSFName name, Color color, BSFSearchable... entries) {
		this.name = name;
		this.color = color;
		this.icon = entries[0].getIcon().copy();
		if (name != null)
			this.icon.toolTipStack.setStackDisplayName("§f" + name.singular());
		this.entries = entries;

		if (name != null)
			addAllIds(this);
	}

	private void addAllIds(Category category) {
		for (BSFSearchable entry : category.entries) {
			if (entry instanceof Category c) {
				addAllIds(c);
			} else if (entry instanceof Biome b) {
				biomeIds.addAll(b.getIds());
			}
		}
	}

	@Override
	public BSFName getName() {
		return name;
	}

	@Override
	public GuiBigChest.TextureItem getIcon() {
		return icon;
	}

	@Override
	public Color getColor() {
		return color;
	}

}
