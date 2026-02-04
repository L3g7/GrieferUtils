/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_info.info_suppliers;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.item.item_info.ItemInfo;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.Collections;
import java.util.List;

@Singleton
public class SpawnEggType extends ItemInfo.ItemInfoSupplier {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spawn-Ei-Typ anzeigen")
		.description("Zeigt unter Spawn-Eiern an, von welchem Typ sie sind.")
		.icon(new ItemStack(Items.spawn_egg, 1, 50 /* Creeper */));

	@Override
	public List<String> getToolTip(ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemMonsterPlacer))
			return Collections.emptyList();

		String entity = EntityList.getStringFromID(itemStack.getMetadata());
		if (entity != null) {
			String translationKey = "entity." + entity + ".name";
			String translatedEntity = StatCollector.translateToLocal("entity." + entity + ".name");
			if (!translatedEntity.equals(translationKey))
				entity = translatedEntity;
		}
		return ImmutableList.of("§fTyp: " + entity);
	}

}
