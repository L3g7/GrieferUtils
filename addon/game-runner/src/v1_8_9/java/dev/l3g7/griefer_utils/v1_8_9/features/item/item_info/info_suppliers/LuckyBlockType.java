/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.item_info.info_suppliers;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.item.item_info.ItemInfo;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class LuckyBlockType extends ItemInfo.ItemInfoSupplier {

	private static final Map<Integer, String> LURE_TO_NAME = new HashMap<Integer, String>() {{
		put(6,  "§4§lUl§c§ltr§4§la-§c§lUn§4§llu§c§lck§4§lyB§c§llo§4§lck");
		put(21, "§4§lMe§c§lga§4§l-U§c§lnl§4§luc§c§lky§4§lBl§c§loc§4§lk");
		put(36, "§4§lSu§c§lpe§4§lr-§c§lUn§4§llu§c§lck§4§lyB§c§llo§4§lck");

		put(66, "§e§lSu§6§lpe§e§lr-§6§lLu§e§lck§6§lyB§e§llo§6§lck");
		put(81, "§e§lMe§6§lga§e§l-L§6§luc§e§lky§6§lBl§e§loc§6§lk");
		put(96, "§e§lUl§6§ltr§e§la-§6§lLu§e§lck§6§lyB§e§llo§6§lck");

		put(28, "§2Ul§atr§2a-§aUn§2lu§ack§2y §2Ad§ave§2nt§aur§2er §e§lLu§6§lck§e§lyb§6§llo§e§lck");
		put(31, "§2Me§aga-§aUn§2lu§ack§2y §2Ad§ave§2nt§aur§2er §e§lLu§6§lck§e§lyb§6§llo§e§lck");
		put(34, "§2Su§ape§2r-§aUn§2lu§ack§2y §2Ad§ave§2nt§aur§2er §e§lLu§6§lck§e§lyb§6§llo§e§lck");
		put(68, "§2Su§ape§2r §2Ad§ave§2nt§aur§2er §e§lLu§6§lck§e§lyb§6§llo§e§lck");
		put(71, "§2Me§aga §2Ad§ave§2nt§aur§2er §e§lLu§6§lck§e§lyb§6§llo§e§lck");
		put(74, "§2Ul§atr§2a §2Ad§ave§2nt§aur§2er §e§lLu§6§lck§e§lyb§6§llo§e§lck");
	}};

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("LuckyBlock-Typ anzeigen")
		.description("Zeigt unter LuckyBlöcken an, von welchem Typ sie sind.")
		.icon(ItemUtil.createItem(Blocks.gold_block, 0, true));

	@Override
	public List<String> getToolTip(ItemStack itemStack) {
		if (!itemStack.hasTagCompound()
			|| itemStack.getTagCompound().getInteger("HideFlags") != 19
			|| EnchantmentHelper.getEnchantments(itemStack).get(51) != -1)
			return Collections.emptyList();

		Integer lure = EnchantmentHelper.getEnchantments(itemStack).get(62);
		return ImmutableList.of("§e§lTyp: " + LURE_TO_NAME.getOrDefault(lure, "§e§lLu§6§lck§e§lyB§6§llo§e§lck"));
	}

}
