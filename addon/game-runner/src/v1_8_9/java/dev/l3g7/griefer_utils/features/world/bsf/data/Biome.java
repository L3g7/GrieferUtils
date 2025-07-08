/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.bsf.data;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public enum Biome implements BSFSearchable {

	OCEAN(new BSFName("n", "Ozean", "e"), new Color(0x000070), Items.water_bucket, 0, 24),
	PLAINS(new BSFName("", "Ebene", "n"), new Color(0x8DB360), Blocks.grass, 1),
		SNOWY_TUNDRA(new BSFName("", "Verschneite Ebene", "Verschneiten Ebenen"), new Color(0xFFFFFF), Blocks.snow_layer, 12),
		SUNFLOWER_PLAINS(new BSFName("", "Sonnenblumenebene", "n"), new Color(0xB5DB88), Blocks.double_plant, 129),
	FROZEN_OCEAN(new BSFName("n", "Vereister Ozean", "Vereisten Ozeane"), new Color(0x7070D6), Blocks.ice, 10),
		ICE_SPIKES(new BSFName("", "Eiszapfentundra", "Eiszapfentundren"), new Color(0xB4DCDC), Blocks.packed_ice, 140),
	DESERT(new BSFName("", "Wüste", "n"), new Color(0xFA9418), Blocks.sand, 2, 17, 130),
	FOREST(new BSFName("n", "Wald", "Wälder"), new Color(0x056621), Blocks.log, 4, 18),
		DARK_FOREST(new BSFName("n", "Dunkler Wald", "Dunklen Wälder"), new Color(0x40511A), new ItemStack(Blocks.log2, 1, 1), 29, 157),
		FLOWER_FOREST(new BSFName("n", "Blumenwald", "Blumenwälder"), new Color(0x2D8E49), Blocks.yellow_flower, 132),
		BIRCH_FOREST(new BSFName("n", "Birkenwald", "Birkenwälder"), new Color(0x307444), new ItemStack(Blocks.log, 1, 2), 27, 28),
		TALL_BIRCH_FOREST(new BSFName("n", "Birken-Urwald", "Birken-Urwälder"), new Color(0x589C6C), new ItemStack(Blocks.log, 1, 2), 155, 156),
	MOUNTAINS(new BSFName("", "Berge", ""), new Color(0x606060), Blocks.stone, 3, 20),
		SNOWY_MOUNTAINS(new BSFName("", "Verschneite Berge", "Verschneiten Berge"), new Color(0xA0A0A0), Blocks.snow_layer, 13),
		GRAVELLY_MOUNTAINS(new BSFName("", "Geröllberge", ""), new Color(0x888888), Blocks.gravel, 131, 162),
	TAIGA(new BSFName("", "Taiga", "s"), new Color(0x0B6659), new ItemStack(Blocks.log, 1, 1), 5, 19),
		SNOWY_TAIGA(new BSFName("", "Verschneite Taiga", "Verschneiten Taigas"), new Color(0x31554A), Blocks.snow_layer, 30, 31, 158),
		WOODED_MOUNTAINS(new BSFName("", "Taiga Hügel", ""), new Color(0x507050), new ItemStack(Blocks.log, 1, 1), 34, 133),
		GIANT_TAIGA(new BSFName("", "Urtaiga", "s"), new Color(0x596651), new ItemStack(Blocks.log, 1, 1), 32, 33, 160, 161),
	SWAMP(new BSFName("n", "Sumpf", "Sümpfe"), new Color(0x07F9B2), Blocks.vine, 6, 134),
	MUSHROOM_FIELDS(new BSFName("s", "Pilzland", "Pilzländer"), new Color(0xFF00FF), Blocks.mycelium, 14, 15),
	BEACH(new BSFName("n", "Strand", "Strände"), new Color(0xFADE55), Blocks.sand, 16),
		STONE_SHORE(new BSFName("n", "Stein Strand", "Stein Strände"), new Color(0xA2A284), Blocks.stone, 25),
		SNOWY_BEACH(new BSFName("n", "Verschneiter Strand", "Verschneiten Strände"), new Color(0xFAF0C0), Blocks.snow_layer, 26),
	JUNGLE(new BSFName("n", "Dschungel", ""), new Color(0x537B09), new ItemStack(Blocks.log, 1, 3), 21, 22, 23, 149, 151),
	SAVANNA(new BSFName("", "Savanne", "n"), new Color(0xBDB25F), Blocks.log2, 35, 36),
		SHATTERED_SAVANNA(new BSFName("", "Zerklüftete Savanne", "Zerklüftete Savannen"), new Color(0xE5DA87), Blocks.log2, 163, 164),
	BADLANDS(new BSFName("", "Tafelberge", ""), new Color(0xD94515), Blocks.hardened_clay, 37, 39, 167),
		WOODED_BADLANDS(new BSFName("", "Bewaldete Tafelberge", "Bewaldeten Tafelberge"), new Color(0xB09765), Blocks.log, 38, 166),
		ERODED_BADLANDS(new BSFName("", "Abgetragene Tafelberge", "Abgetragenen Tafelberge"), new Color(0xFF6D3D), new ItemStack(Blocks.stained_hardened_clay, 1, 1), 165);

	private final BSFName name;
	private final ItemStack icon;
	private final Color color;
	private final List<Integer> ids;

	Biome(BSFName name, Color color, Object icon, int... ids) {
		this.name = name;
		this.color = color;
		this.icon = BSFSearchable.toStack(icon);
		this.icon.setStackDisplayName("§f" + name.singular);
		this.ids = new ArrayList<>(ids.length);
		for (int id : ids) {
			this.ids.add(id);
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

	public List<Integer> getIds() {
		return ids;
	}

}