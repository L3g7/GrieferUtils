/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.render.BurningCheckEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderPortalCheckEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderPortalDistortionEvent;
import dev.l3g7.griefer_utils.core.events.render.SetupFogEvent;
import dev.l3g7.griefer_utils.core.events.render.SetupFogEvent.FogType;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static net.minecraft.init.Blocks.stained_glass_pane;

/**
 * Deactivates some overlays.
 */
@Singleton
public class NoOverlay extends Feature {

	private final SwitchSetting blindness = SwitchSetting.create()
		.name("Blindheit entfernen")
		.description("Deaktiviert den Blindheits-Effekt.")
		.icon(new ItemStack(stained_glass_pane, 1, 15))
		.defaultValue(true);

	private final SwitchSetting water = SwitchSetting.create()
		.name("Wassertrübheit entfernen")
		.description("Deaktiviert die Wassertrübheit.")
		.icon(new ItemStack(stained_glass_pane, 1, 3))
		.defaultValue(true);

	private final SwitchSetting lava = SwitchSetting.create()
		.name("Lavatrübheit entfernen")
		.description("Deaktiviert die Lavatrübheit.")
		.icon(new ItemStack(stained_glass_pane, 1, 1))
		.defaultValue(true);

	private final SwitchSetting nausea = SwitchSetting.create()
		.name("Übelkeit entfernen")
		.description("Deaktiviert die Übelkeit.")
		.icon(new ItemStack(stained_glass_pane, 1, 13))
		.defaultValue(true);

	private final SwitchSetting portal = SwitchSetting.create()
		.name("Portal-Effekt entfernen")
		.description("Deaktiviert den Portal-Effekt.")
		.icon(new ItemStack(stained_glass_pane, 1, 2))
		.defaultValue(true);

	private final SwitchSetting fire = SwitchSetting.create()
		.name("Feuer-Overlay entfernen")
		.description("§r§fDeaktiviert den Feuer-Effekt im First-Person-Modus.", "§l§nNur benutzen, wenn man Feuerresistenz besitzt!")
		.icon("fire");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Overlays entfernen")
		.description("Entfernt einige Overlays.")
		.icon(new ItemStack(stained_glass_pane))
		.subSettings(blindness, water, lava, nausea, portal, fire);

	@EventListener
	private void onDisplayNameRender(SetupFogEvent event) {
		if (event.fogType == FogType.BLINDNESS) {
			if (blindness.get()) event.cancel();
		} else if (event.fogType == FogType.WATER) {
			if (water.get()) event.cancel();
		} else if (event.fogType == FogType.LAVA) {
			if (lava.get()) event.cancel();
		}
	}

	@EventListener
	public void onPortalDistortionRender(RenderPortalDistortionEvent event) {
		if (nausea.get())
			event.distortion = 0;
	}

	@EventListener
	private void onPortalRender(RenderPortalCheckEvent event) {
		if (portal.get())
			event.cancel();
	}

	@EventListener
	public void onDisplayNameRender(BurningCheckEvent event) {
		// Only hide if in first person
		if (event.burning && mc().gameSettings.thirdPersonView == 0 && fire.get() && event.entity == player())
			event.burning = false;
	}

}
