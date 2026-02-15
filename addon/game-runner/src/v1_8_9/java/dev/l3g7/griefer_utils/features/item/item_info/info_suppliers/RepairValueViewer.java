/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_info.info_suppliers;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.ItemTooltipEvent;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.misc.gui.guis.GuiBigChest;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.widgets.other.BlockInfo;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;

@Singleton
public class RepairValueViewer extends Feature {

	private final StringSetting format = StringSetting.create()
		.name("Format")
		.description("In welchem Format der Reparaturwert angezeigt werden soll.\n(\\n wird durch einen Zeilenumbruch und %s durch den Reparaturwert ersetzt)")
		.icon("book_and_quill")
		.defaultValue("\\n&7Reparaturwert: %s")
		.validator(v -> {
			try {
				String.format(v, "");
				return v.contains("%s");
			} catch (IllegalFormatException e) {
				return false;
			}
		});

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Reparaturwert anzeigen")
		.description("Zeigt unter einem Item seinen Reparaturwert (wie viele XP-Level eine Reparatur mindestens kostet) an.")
		.icon("weakness")
		.subSettings(format);

	@EventListener
	public void onTooltip(ItemTooltipEvent e) {
		if (FileProvider.getSingleton(BlockInfo.class).gettingTooltip)
			return;

		if (MinecraftUtil.mc().currentScreen instanceof GuiBigChest)
			return;

		int cost = e.itemStack.getRepairCost();

		String text = String.format(DrawUtils.createColors(format.get()), "§r§" + getColor(e.itemStack) + cost + "§r");
		List<String> lines = Arrays.asList(text.split("\\\\n"));
		lines.replaceAll(s -> "§r" + s);
		e.toolTip.addAll(lines);
	}

	/**
	 *  0   - Green
	 * 1-39 - Yellow
	 * ≥ 40 - Red
	 */
	private char getColor(ItemStack itemStack) {
		if (itemStack.getRepairCost() < 1)
			return 'a';

		return ItemUtil.canBeRepaired(itemStack) ? 'e' : 'c';
	}

}
