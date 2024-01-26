/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.render.BurningCheckEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.render.RenderPortalCheckEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.render.RenderPortalDistortionEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.render.SetupFogEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.render.SetupFogEvent.FogType;
import dev.l3g7.griefer_utils.v1_8_9.features.Feature;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;
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
