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

public enum Biome implements BSFSearchable {

	OCEAN(new BSFName("n", "Ozean", "e"), new Color(0x000070), 0, 24),
	PLAINS(new BSFName("", "Ebene", "n"), new Color(0x8DB360), 1),
		SNOWY_TUNDRA(new BSFName("", "Verschneite Ebene", "Verschneiten Ebenen"), new Color(0xFFFFFF), 12),
		SUNFLOWER_PLAINS(new BSFName("", "Sonnenblumenebene", "n"), new Color(0xB5DB88), 129),
	ICE_SPIKES(new BSFName("", "Eiszapfentundra", "Eiszapfentundren"), new Color(0xB4DCDC), 140),
	DESERT(new BSFName("", "Wüste", "n"), new Color(0xFA9418), 2, 17, 130),
	FOREST(new BSFName("n", "Wald", "Wälder"), new Color(0x056621), 4, 18),
		DARK_FOREST(new BSFName("n", "Dunkler Wald", "Dunklen Wälder"), new Color(0x40511A), 29, 157),
		FLOWER_FOREST(new BSFName("n", "Blumenwald", "Blumenwälder"), new Color(0x2D8E49), 132),
		BIRCH_FOREST(new BSFName("n", "Birkenwald", "Birkenwälder"), new Color(0x307444), 27, 28),
		TALL_BIRCH_FOREST(new BSFName("n", "Birken-Urwald", "Birken-Urwälder"), new Color(0x589C6C), 155, 156),
	MOUNTAINS(new BSFName("", "Berge", ""), new Color(0x606060), 3, 20),
		SNOWY_MOUNTAINS(new BSFName("", "Verschneite Berge", "Verschneiten Berge"), new Color(0xA0A0A0), 13),
		GRAVELLY_MOUNTAINS(new BSFName("", "Geröllberge", ""), new Color(0x888888), 131, 162),
	TAIGA(new BSFName("", "Taiga", "s"), new Color(0x0B6659), 5, 19, 34, 133),
		SNOWY_TAIGA(new BSFName("", "Verschneite Taiga", "Verschneiten Taigas"), new Color(0x31554A), 30, 31, 158),
		GIANT_TAIGA(new BSFName("", "Urtaiga", "s"), new Color(0x596651), 32, 33, 160, 161),
	SWAMP(new BSFName("n", "Sumpf", "Sümpfe"), new Color(0x07F9B2), 6, 134),
	MUSHROOM_FIELDS(new BSFName("s", "Pilzland", "Pilzländer"), new Color(0xFF00FF), 14, 15),
	BEACH(new BSFName("n", "Strand", "Strände"), new Color(0xFADE55), 16),
		STONE_SHORE(new BSFName("n", "Stein Strand", "Stein Strände"), new Color(0xA2A284), 25),
		SNOWY_BEACH(new BSFName("n", "Verschneiter Strand", "Verschneiten Strände"), new Color(0xFAF0C0), 26),
	JUNGLE(new BSFName("n", "Dschungel", ""), new Color(0x537B09), 21, 22, 23, 149, 151),
	SAVANNA(new BSFName("", "Savanne", "n"), new Color(0xBDB25F), 35, 36, 163, 164),
	BADLANDS(new BSFName("", "Tafelberge", ""), new Color(0xD94515), 37, 38, 39, 165, 166, 167);

	private final BSFName name;
	private final GuiBigChest.TextureItem icon;
	private final Color color;
	private final List<Integer> ids;

	Biome(BSFName name, Color color, int... ids) {
		this.name = name;
		this.color = color;
		this.icon = new GuiBigChest.TextureItem("biomes/" + name().toLowerCase(), "§f" + name.singular());
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
	public GuiBigChest.TextureItem getIcon() {
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