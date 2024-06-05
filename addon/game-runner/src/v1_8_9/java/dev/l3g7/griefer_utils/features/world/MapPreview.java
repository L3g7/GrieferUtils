/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.world;


import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.render.RenderToolTipEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.core.util.render.RenderUtil;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.world.storage.MapData;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class MapPreview extends Feature {

	private final NumberSetting size = NumberSetting.create()
		.name("Vergrößerungs-Faktor")
		.description("Um wie viel die Karte in der Vorschau vergrößert werden soll.")
		.icon("magnifying_glass")
		.defaultValue(3)
		.min(2)
		.max(16);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Karten-Vorschau")
		.description("Zeigt in der Beschreibung von Karten eine Vorschau an.")
		.icon(Items.map)
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