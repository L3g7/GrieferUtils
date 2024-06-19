/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftBridge;
import net.minecraft.init.Items;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

/**
 * Original version by Pleezon
 */
@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class RecraftBridgeImpl implements RecraftBridge {

	private final RecraftPieMenu pieMenu = new RecraftPieMenu();

	public static final RecraftPage.RecraftPageListSetting pages = new RecraftPage.RecraftPageListSetting()
		.name("Seiten")
		.icon(Items.map);

	@Override
	public void openPieMenu(boolean animation) {
		pieMenu.open(animation, pages);
	}

	@Override
	public void closePieMenu() {
		pieMenu.close();
	}

	@Override
	public BaseSetting<?> getPagesSetting() {
		return pages;
	}

	@Override
	public dev.l3g7.griefer_utils.features.item.recraft.RecraftRecording createEmptyRecording() {
		return new RecraftRecording("Leere Aufzeichnung");
	}

	@Override
	public void init() {
		RecraftBridge.super.init();
	}

}
