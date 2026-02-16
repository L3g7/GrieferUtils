/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.clean_up_player_menu.impl;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.features.player.clean_up_player_menu.CleanUpPlayerMenu.CleanUpPlayerMenuBridge;
import net.labymod.main.LabyMod;
import net.labymod.user.util.UserActionEntry;

import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

@Singleton
@ExclusiveTo(LABY_3)
public class CleanUpPlayerMenuBridgeLaby3 extends CleanUpPlayerMenuBridge<UserActionEntry> {

	@Override
	protected List<UserActionEntry> getEntriesReference() {
		return Reflection.get(LabyMod.getInstance().getUserManager().getUserActionGui(), "defaultEntries");
	}

	@Override
	protected String getName(UserActionEntry entry) {
		return entry.getDisplayName();
	}

	@Override
	@OnStartupComplete
	public void init() {
		super.init();
	}

}
