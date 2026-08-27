/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.gui.griefer_games;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.primitives.containers.Option;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.misc.griefer_games.Citybuild;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.init.Items;

@Singleton
public class InteractableFriendsMenu extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Interagierbare /freunde")
		.description("Ermöglicht das Joinen von CBs durch das Linksklicken eines Spielers im /freunde-Menü.")
		.icon("players")
		.since("2.4-BETA-1");

	@EventListener
	private void onGuiClick(WindowClickEvent event) {
		if (!event.windowTitle.startsWith("§6Freunde - Menü"))
			return;

		// Only trigger on left clicks
		if ((event.mode != 0 && event.mode != 1) || event.mouseButtonClicked != 0)
			return;

		if (event.itemStack == null || event.itemStack.getItem() != Items.skull)
			return;

		String serverLine = ItemUtil.getLoreAtIndex(event.itemStack, 1);
		if (!serverLine.startsWith("§7Server: §e"))
			return;

		Option<Citybuild> parsedCB = Citybuild.tryParse(serverLine.substring("§7Server: §e".length()));
		if (parsedCB.isUnset())
			return;

		event.cancel();
		MinecraftUtil.closeServersideGUI();
		parsedCB.get().join();
	}

}
