/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.clean_up_player_menu.impl;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.features.player.clean_up_player_menu.CleanUpPlayerMenu.CleanUpPlayerMenuBridge;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.interaction.BulletPoint;
import net.labymod.api.util.KeyValue;
import net.minecraft.util.IChatComponent;

import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@Singleton
@ExclusiveTo(LABY_4)
public class CleanUpPlayerMenuBridgeLaby4 extends CleanUpPlayerMenuBridge<KeyValue<BulletPoint>> {

	@Override
	protected List<KeyValue<BulletPoint>> getEntriesReference() {
		return Laby.references().interactionMenuRegistry().getElements();
	}

	@Override
	protected String getName(KeyValue<BulletPoint> entry) {
		return ((IChatComponent) entry.getValue().getTitle()).getUnformattedText();
	}

	@Override
	@OnStartupComplete
	public void init() {
		super.init();
	}

}
