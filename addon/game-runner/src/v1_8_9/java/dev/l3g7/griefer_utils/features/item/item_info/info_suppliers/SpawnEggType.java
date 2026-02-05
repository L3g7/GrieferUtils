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
import dev.l3g7.griefer_utils.core.misc.gui.guis.GuiBigChest;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.widgets.other.BlockInfo;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

@Singleton
public class SpawnEggType extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spawn-Ei-Typ anzeigen")
		.description("Zeigt unter Spawn-Eiern an, von welchem Typ sie sind.")
		.icon(new ItemStack(Items.spawn_egg, 1, 50 /* Creeper */));

	@EventListener
	public void onTooltip(ItemTooltipEvent e) {
		if (FileProvider.getSingleton(BlockInfo.class).gettingTooltip)
			return;

		if (MinecraftUtil.mc().currentScreen instanceof GuiBigChest)
			return;

		if (!(e.itemStack.getItem() instanceof ItemMonsterPlacer))
			return;

		String entity = EntityList.getStringFromID(e.itemStack.getMetadata());
		if (entity != null) {
			String translationKey = "entity." + entity + ".name";
			String translatedEntity = StatCollector.translateToLocal("entity." + entity + ".name");
			if (!translatedEntity.equals(translationKey))
				entity = translatedEntity;
		}
		e.toolTip.add("§fTyp: " + entity);
	}

}
