/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.gui.integrations.bsf.data;

import dev.l3g7.griefer_utils.core.misc.gui.guis.GuiBigChest;

import java.awt.*;

public enum Structure implements BSFSearchable {

	MONUMENT(new BSFName("s", "Ozeanmonument", "e"), new Color(0x00F2FF), "mob_icons/faithless/guardian_1.21"),
	MINESHAFT(new BSFName("", "Mine", "n"), new Color(0xDBA472), "mob_icons/faithless/minecartchest_1.21"),
	PYRAMID(new BSFName("", "Pyramide", "n"), new Color(0xFFDF7F), "structures/desert_pyramid"),
	JUNGLE_TEMPLE(new BSFName("n", "Dschungeltempel", ""), new Color(0x00B718), "structures/jungle_temple"),
	VILLAGE(new BSFName("s", "Dorf", "Dörfer"), new Color(0xC6884D), "structures/village"),
	STRONGHOLD(new BSFName("", "Festung", "en"), new Color(0x969696), "structures/stronghold"),
	SWAMP_HUT(new BSFName("", "Sumpfhütte", "n"), new Color(0x60D69D), "mob_icons/faithless/witch");

	private final BSFName name;
	private final GuiBigChest.TextureItem stack;
	private final Color color;

	Structure(BSFName name, Color color, String icon) {
		this.name = name;
		this.color = color;
		this.stack = new GuiBigChest.TextureItem(icon, "§f" + name.singular());
	}

	@Override
	public BSFName getName() {
		return name;
	}

	@Override
	public GuiBigChest.TextureItem getIcon() {
		return stack;
	}

	@Override
	public Color getColor() {
		return color;
	}

}
