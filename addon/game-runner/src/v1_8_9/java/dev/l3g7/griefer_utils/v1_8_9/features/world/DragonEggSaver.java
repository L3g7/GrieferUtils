/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.world;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.BlockEvent.BlockClickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.BlockEvent.BlockInteractEvent;
import net.minecraft.init.Blocks;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.world;

@Singleton
public class DragonEggSaver extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Drachenei-Saver")
		.description("Verhindert Klicks auf Dracheneier.")
		.icon(Blocks.dragon_egg);

	@EventListener
	private void onBlockInteract(BlockInteractEvent event) {
		if (world().getBlockState(event.pos).getBlock() == Blocks.dragon_egg)
			event.cancel();
	}

	@EventListener
	private void onBlockClick(BlockClickEvent event) {
		if (world().getBlockState(event.pos).getBlock() == Blocks.dragon_egg)
			event.cancel();
	}

}
