/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.render.RenderBarrierCheckEvent;
import net.minecraft.init.Blocks;

/**
 * Shows barriers.
 */
@Singleton
public class ShowBarriers extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Barrieren anzeigen")
		.description("Fügt Partikel bei Barrieren-Blöcken hinzu.")
		.icon(Blocks.barrier)
		.addHotkeySetting("das Anzeigen von Barrieren", null);

	@EventListener
	public void onDisplayNameRender(RenderBarrierCheckEvent event) {
		event.renderBarrier = true;
	}

}
