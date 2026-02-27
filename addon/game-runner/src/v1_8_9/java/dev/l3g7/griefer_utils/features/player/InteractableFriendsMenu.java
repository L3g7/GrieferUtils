/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.init.Items;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class InteractableFriendsMenu extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Interagierbare /freunde")
		.description("Ermöglicht das Joinen von CBs durch das Linksklicken eines Spielers im /freunde-Menü")
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

		String citybuild = ItemUtil.getLoreAtIndex(event.itemStack, 1).substring("§7Server: §e".length());
		Citybuild parsedCB = Citybuild.getCitybuild(citybuild);
		if (parsedCB == Citybuild.ANY)
			return;

		event.cancel();
		mc().displayGuiScreen(null);
		parsedCB.join();
	}

}
