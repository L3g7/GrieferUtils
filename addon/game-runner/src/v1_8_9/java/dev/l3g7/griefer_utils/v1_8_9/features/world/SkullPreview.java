/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.world;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.render.RenderToolTipEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class SkullPreview extends Feature {

	private final NumberSetting size = NumberSetting.create()
		.name("Vergrößerungs-Faktor")
		.description("Um wie viel der Kopf in der Vorschau vergrößert werden soll.")
		.icon("magnifying_glass")
		.defaultValue(4)
		.min(2)
		.max(16);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Kopf-Vorschau")
		.description("Zeigt in der Beschreibung von Köpfen eine vergrößerte Version an.")
		.icon("steve")
		.subSettings(size);

	@EventListener
	public void onTooltipRender(RenderToolTipEvent event) {
		if (event.stack.getItem() != Items.skull || event.stack.getMetadata() != 3)
			return;

		RenderUtil.renderToolTipWithLeftPadding(event, 12 * size.get(), (left, top) -> {
			GlStateManager.translate(left - (size.get() * 2), top - (size.get() * 2), 1);
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.scale(size.get(), size.get(), 1);
			mc().getRenderItem().zLevel = 750;
			mc().getRenderItem().renderItemAndEffectIntoGUI(event.stack, 0, 0);
		});

	}

}
