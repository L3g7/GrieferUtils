/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.other;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.ItemCounter;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@Singleton
public class CompressedBlockCounter extends SimpleWidget {

	private final DropDownSetting<ItemCounter.FormatMode> formatting = DropDownSetting.create(ItemCounter.FormatMode.class)
		.name("Formattierung")
		.description("In welchem Format die Anzahl angezeigt werden soll.")
		.icon("XZRF:color_palette")
		.defaultValue(ItemCounter.FormatMode.UNFORMATTED);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Komprimierter Block")
		.description("Zeigt dir an, wie viele Blöcke noch in dem komprimierten Block sind, der in der Hand gehalten wird.")
		.icon("XZRF:bundle")
		.subSettings(formatting);

	@Override
	public boolean isVisibleInGame() {
		if (player() == null)
			return false;

		ItemStack heldItem = player().getHeldItem();
		return heldItem != null && ItemUtil.getCompressionLevel(heldItem) > 0;
	}

	@Override
	public String getValue() {
		if (player() == null)
			return "";

		ItemStack heldItem = player().getHeldItem();
		if (heldItem == null)
			return "";

		return getFormattedAmount(ItemUtil.getDecompressedAmount(heldItem), heldItem.getMaxStackSize());
	}

	private String getFormattedAmount(long amount, int stackSize) {
		String formatString = "";
		if (formatting.get() != ItemCounter.FormatMode.UNFORMATTED) formatString += ItemCounter.formatAmount(amount, stackSize);
		if (formatting.get() == ItemCounter.FormatMode.BOTH) formatString += " / ";
		if (formatting.get() != ItemCounter.FormatMode.FORMATTED) formatString += Constants.DECIMAL_FORMAT_98.format(amount);

		return formatString;
	}

}