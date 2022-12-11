/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.render.SetupFogEvent;
import dev.l3g7.griefer_utils.event.events.render.SetupFogEvent.FogType;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.item.ItemStack;

import static net.minecraft.init.Blocks.stained_glass_pane;

/**
 * Deactivates some fogs.
 */
@Singleton
public class NoFog extends Feature {

	private final BooleanSetting blindness = new BooleanSetting()
		.name("Keine Blindheit")
		.description("Deaktiviert den Blindheits-Effekt.")
		.icon(new ItemStack(stained_glass_pane, 1, 15));

	private final BooleanSetting water = new BooleanSetting()
		.name("Keine Wassertrübheit")
		.description("Deaktiviert die Wassertrübheit.")
		.icon(new ItemStack(stained_glass_pane, 1, 3));

	private final BooleanSetting lava = new BooleanSetting()
		.name("Keine Lavatrübheit")
		.description("Deaktiviert die Lavatrübheit.")
		.icon(new ItemStack(stained_glass_pane, 1, 1));

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Kein Nebel")
		.description("Entfernt einige Nebel-Effekte.")
		.icon(new ItemStack(stained_glass_pane))
		.subSettings(blindness, water, lava);

	@EventListener
	public void onDisplayNameRender(SetupFogEvent event) {
		if (event.fogType == FogType.BLINDNESS)
			event.setCanceled(blindness.get());
		else if (event.fogType == FogType.WATER)
			event.setCanceled(water.get());
		else if (event.fogType == FogType.LAVA)
			event.setCanceled(lava.get());
	}

}
