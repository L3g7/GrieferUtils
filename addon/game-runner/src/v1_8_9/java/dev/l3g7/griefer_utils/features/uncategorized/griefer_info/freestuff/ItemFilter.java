/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.griefer_info.freestuff;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.*;

import static dev.l3g7.griefer_utils.core.util.ItemUtil.createItem;
import static net.minecraft.init.Blocks.*;
import static net.minecraft.init.Items.*;

public class ItemFilter {

	public static Map<String, ItemFilter> FILTER = new HashMap<>();
	public static Map<String, ItemFilter> TYPES_BY_ID = new HashMap<>();
	public static final List<Category> CATEGORIES = Arrays.asList(Category.values());

	public final String germanName;
	public final String[] otherNames;
	public final ItemStack stack;
	public String id;

	public ItemFilter(String germanName, ItemStack stack, boolean customName) {
		this.germanName = germanName;
		if (customName) {
			otherNames = new String[0];
		} else {
			otherNames = new String[] {
				StatCollector.translateToLocal(stack.getUnlocalizedName() + ".name").toLowerCase(),
				StatCollector.translateToFallback(stack.getUnlocalizedName() + ".name").toLowerCase()
			};
		}
		stack.setStackDisplayName("§6§n" + germanName);

		this.stack = stack;
	}

	public void setId(String id) {
		this.id = id;
		TYPES_BY_ID.put(id, this);
	}

	public boolean matchesFilter(String filter) {
		filter = filter.toLowerCase();

		for (String otherName : otherNames)
			if (otherName.contains(filter))
				return true;

		return germanName.toLowerCase().contains(filter);
	}


	public enum Category {

		BUILDING(brick_block, "Baublöcke"),
		NATURE(grass, "Naturblöcke"),
		FUNCTIONAL(crafting_table, "Funktionale Blöcke"),
		COMBAT(iron_sword, "Kampf"),
		TOOLS(diamond_pickaxe, "Werkzeuge & Hilfsmittel"),
		FOOD(golden_apple, "Nahrung"),
		INGREDIENTS(stick, "Werkstoffe"),
		MINERALS(diamond, "Minerale"),
		MOB(createItem(Items.skull, 2, "§6§nMob")),
		ADMIN(beacon, "Admin-Items");

		public final ItemStack stack;
		public final List<ItemFilter> itemFilters = new ArrayList<>();

		Category(Block block, String name) {
			this(createItem(block, 0, "§6§n" + name));
		}

		Category(Item item, String name) {
			this(createItem(item, 0, "§6§n" + name));
		}

		Category(ItemStack stack) {
			this.stack = stack;
		}

	}

}
