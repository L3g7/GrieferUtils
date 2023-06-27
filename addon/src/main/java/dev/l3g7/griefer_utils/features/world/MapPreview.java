/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.world;


import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.render.RenderToolTipEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.labymod.utils.Material;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.world.storage.MapData;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class MapPreview extends Feature {

	private final NumberSetting size = new NumberSetting()
		.name("Vergrößerungs-Faktor")
		.description("Um wie viel die Karte in der Vorschau vergrößert werden soll.")
		.icon("magnifying_glass")
		.defaultValue(3)
		.min(2)
		.max(16);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Karten-Vorschau")
		.description("Zeigt in der Beschreibung von Karten eine Vorschau an.")
		.icon(Material.MAP)
		.subSettings(size);

	@EventListener
	public void onTooltipRender(RenderToolTipEvent event) {
		if (!(event.stack.getItem() instanceof ItemMap))
			return;

		MapData mapData = Items.filled_map.getMapData(event.stack, MinecraftUtil.world());
		if (mapData == null)
			return;

		RenderUtil.renderToolTipWithLeftPadding(event, 17 * size.get(), (left, top) -> {
			GlStateManager.translate(left + (size.get() / 2f), top + (size.get() / 2f), 750);
			EntityRenderer renderer = mc().entityRenderer;

			double mapScale = (16 * size.get()) / 128d;
			GlStateManager.scale(mapScale, mapScale, 1);

			// Enable lighting (otherwise it's darker than it should be)
			renderer.enableLightmap();
			renderer.getMapItemRenderer().renderMap(mapData, false);
			renderer.disableLightmap();
		});
	}

}