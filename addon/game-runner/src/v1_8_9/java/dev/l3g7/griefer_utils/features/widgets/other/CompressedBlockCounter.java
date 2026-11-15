/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.other;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.item.item_info.ItemCounter;
import dev.l3g7.griefer_utils.features.item.item_info.ItemCounter.FormatMode;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.features.item.item_info.ItemCounter.FormatMode.*;

@Singleton
public class CompressedBlockCounter extends SimpleWidget {

	private final DropDownSetting<FormatMode> formatting = DropDownSetting.create(FormatMode.class)
		.name("Formattierung")
		.description("In welchem Format die Anzahl angezeigt werden soll.")
		.icon("color_palette")
		.defaultValue(UNFORMATTED);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Komprimierter Block")
		.description("Zeigt dir an, wie viele Blöcke noch in dem komprimierten Block sind, der in der Hand gehalten wird.")
		.icon("bundle")
		.subSettings(formatting)
		.since("2.4-BETA-1");

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
		if (formatting.get() != UNFORMATTED) formatString += ItemCounter.formatAmount(amount, stackSize);
		if (formatting.get() == BOTH) formatString += " / ";
		if (formatting.get() != FORMATTED) formatString += DECIMAL_FORMAT_98.format(amount);

		return formatString;
	}

}