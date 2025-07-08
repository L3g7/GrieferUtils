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

public enum Structure implements BSFSearchable {

	MONUMENT(new BSFName("s", "Ozeanmonument", "e"), new Color(0x00F2FF), new ItemStack(Blocks.prismarine, 1, 1)),
	MINESHAFT(new BSFName("", "Mine", "n"), new Color(0xDBA472), new ItemStack(Blocks.rail)),
	PYRAMID(new BSFName("", "Pyramide", "n"), new Color(0xFFDF7F), new ItemStack(Blocks.sandstone)),
	JUNGLE_TEMPLE(new BSFName("n", "Dschungeltempel", ""), new Color(0x00B718), new ItemStack(Blocks.leaves, 1, 3)),
	VILLAGE(new BSFName("s", "Dorf", "Dörfer"), new Color(0xC6884D), new ItemStack(Items.spawn_egg, 1, 120)),
	STRONGHOLD(new BSFName("", "Festung", "en"), new Color(0x969696), new ItemStack(Items.ender_eye)),
	SWAMP_HUT(new BSFName("", "Sumpfhütte", "n"), new Color(0x60D69D), new ItemStack(Items.cauldron));

	private final BSFName name;
	private final ItemStack stack;
	private final Color color;

	Structure(BSFName name, Color color, ItemStack stack) {
		this.name = name;
		this.color = color;
		this.stack = stack;
		stack.setStackDisplayName("§f" + name.singular);
	}

	@Override
	public BSFName getName() {
		return name;
	}

	@Override
	public ItemStack getIcon() {
		return stack;
	}

	@Override
	public Color getColor() {
		return color;
	}

}
