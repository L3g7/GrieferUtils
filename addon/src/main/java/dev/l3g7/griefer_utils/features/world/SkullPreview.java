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
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class SkullPreview extends Feature {

	private final NumberSetting size = new NumberSetting()
		.name("Vergrößerungs-Faktor")
		.description("Um wie viel der Kopf in der Vorschau vergrößert werden soll.")
		.icon("magnifying_glass")
		.defaultValue(4)
		.min(2)
		.max(16);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Kopf Vorschau")
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
