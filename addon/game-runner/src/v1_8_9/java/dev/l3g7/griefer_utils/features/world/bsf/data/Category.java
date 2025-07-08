/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.bsf.data;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public enum Category implements BSFSearchable {

	PLAINS(new BSFName("", "Ebene", "n"), new Color(0x8DB360), Blocks.grass, Biome.PLAINS, Biome.SNOWY_TUNDRA, Biome.SUNFLOWER_PLAINS),
	BIRCH(new BSFName("n", "Birkenwald", "Birkenwälder"), new Color(0x589C6C), new ItemStack(Blocks.log, 1, 2), Biome.BIRCH_FOREST, Biome.TALL_BIRCH_FOREST),
	FOREST(new BSFName("n", "Wald", "Wälder"), new Color(0x056621), Blocks.log, Biome.FOREST, Biome.DARK_FOREST, Biome.FLOWER_FOREST, BIRCH),
	MOUNTAINS(new BSFName("", "Berge", ""), new Color(0x606060), Blocks.stone, Biome.MOUNTAINS, Biome.SNOWY_MOUNTAINS, Biome.GRAVELLY_MOUNTAINS),
	TAIGA(new BSFName("", "Taiga", "s"), new Color(0x0B6659), new ItemStack(Blocks.log, 1, 1), Biome.TAIGA, Biome.WOODED_MOUNTAINS, Biome.SNOWY_TAIGA, Biome.GIANT_TAIGA),
	BEACH(new BSFName("n", "Strand", "Strände"), new Color(0xFADE55), Blocks.sand, Biome.BEACH, Biome.SNOWY_BEACH, Biome.STONE_SHORE),
	SAVANNA(new BSFName("", "Savanne", "n"), new Color(0xBDB25F), Blocks.log2, Biome.SAVANNA, Biome.SHATTERED_SAVANNA),
	BADLANDS(new BSFName("", "Tafelberge", ""), new Color(0xD94515), Blocks.hardened_clay, Biome.BADLANDS, Biome.ERODED_BADLANDS, Biome.WOODED_BADLANDS),
	ALL(null, null, null, Biome.OCEAN, Category.PLAINS, Biome.ICE_SPIKES, Biome.DESERT, Category.FOREST, Category.MOUNTAINS, Category.TAIGA, Biome.SWAMP, Biome.MUSHROOM_FIELDS, Category.BEACH, Biome.JUNGLE, Category.SAVANNA, Category.BADLANDS),
	ALL_STRUCTURES(null, null, null, Structure.values());

	public final BSFSearchable[] entries;
	public final List<Integer> biomeIds = new ArrayList<>();
	private final BSFName name;
	private final ItemStack icon;
	private final Color color;

	Category(BSFName name, Color color, Object icon, BSFSearchable... entries) {
		this.name = name;
		this.color = color;
		this.icon = BSFSearchable.toStack(icon);
		if (icon != null)
			this.icon.setStackDisplayName("§f" + name.singular);
		this.entries = entries;

		if (name != null)
			addAllIds(this);
	}

	private void addAllIds(Category category) {
		for (BSFSearchable entry : category.entries) {
			if (entry instanceof Category c) {
				addAllIds(c);
			} else if (entry instanceof Biome b) {
				for (int id : b.getIds()) {
					biomeIds.add(id);
				}
			}
		}
	}

	@Override
	public BSFName getName() {
		return name;
	}

	@Override
	public ItemStack getIcon() {
		return icon;
	}

	@Override
	public Color getColor() {
		return color;
	}

}
