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


import com.google.common.base.Strings;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.utils.Material;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class MapPreview extends Feature {

	private List<String> tooltip = new ArrayList<>();

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Karten Vorschau")
		.description("Zeigt in der Beschreibung von Karten eine Vorschau an.")
		.icon(Material.MAP)
		.defaultValue(false);

	@EventListener(priority = EventPriority.LOWEST)
	public void onTooltip(ItemTooltipEvent event) {
		if (!(event.itemStack.getItem() instanceof ItemMap))
			return;

		// Add space(s)
		int lines = (int) Math.ceil(48 / (mc().fontRendererObj.FONT_HEIGHT + 1d));
		int spaces = (int) Math.ceil(48 / (double) mc().fontRendererObj.getCharWidth(' '));

		for (int i = 0; i < lines; i++)
			event.toolTip.add(Strings.repeat(" ", spaces));

		tooltip = new ArrayList<>(event.toolTip);
	}

	@EventListener
	public void onRender(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (!(event.gui instanceof GuiContainer))
			return;

		// If a stack is being held by the mouse, no tooltip is shown
		if (MinecraftUtil.player().inventory.getItemStack() != null)
			return;

		GuiContainer currentScreen = (GuiContainer) event.gui;
		Slot slot = currentScreen.getSlotUnderMouse();
		if (slot == null || !slot.getHasStack())
			return;

		ItemStack stack = slot.getStack();
		if (!(stack.getItem() instanceof ItemMap))
			return;

		MapData mapData = Items.filled_map.getMapData(stack, MinecraftUtil.world());
		if (mapData == null)
			return;

		EntityRenderer renderer = mc().entityRenderer;

		// Enable lighting (otherwise it's darker than it should be)
		renderer.enableLightmap();
		renderMap(event.mouseX, event.mouseY, currentScreen.width, currentScreen.height, mc().fontRendererObj, mapData);
		renderer.disableLightmap();
	}

	/**
	 * Source of the code to calculate the position: {@link net.minecraftforge.fml.client.config.GuiUtils#drawHoveringText(List, int, int, int, int, int, FontRenderer) GuiUtils.drawHoveringText}
	 */
	private void renderMap(int mouseX, int mouseY, int screenWidth, int screenHeight, FontRenderer font, MapData mapData) {
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		int tooltipTextWidth = 0;

		for (String textLine : tooltip) {
			int textLineWidth = font.getStringWidth(textLine);

			if (textLineWidth > tooltipTextWidth)
				tooltipTextWidth = textLineWidth;
		}

		boolean needsWrap = false;

		int titleLinesCount = 1;
		int tooltipX = mouseX + 12;
		if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
			tooltipX = mouseX - 16 - tooltipTextWidth;
			if (tooltipX < 4) { // if the tooltip doesn't fit on the screen
				if (mouseX > screenWidth / 2)
					tooltipTextWidth = mouseX - 12 - 8;
				else
					tooltipTextWidth = screenWidth - 16 - mouseX;

				needsWrap = true;
			}
		}

		if (needsWrap) {
			int wrappedTooltipWidth = 0;
			List<String> wrappedTextLines = new ArrayList<>();
			for (int i = 0; i < tooltip.size(); i++) {
				String textLine = tooltip.get(i);
				List<String> wrappedLine = font.listFormattedStringToWidth(textLine, tooltipTextWidth);

				if (i == 0)
					titleLinesCount = wrappedLine.size();

				for (String line : wrappedLine) {
					int lineWidth = font.getStringWidth(line);
					if (lineWidth > wrappedTooltipWidth)
						wrappedTooltipWidth = lineWidth;

					wrappedTextLines.add(line);
				}
			}
			tooltipTextWidth = wrappedTooltipWidth;
			tooltip = wrappedTextLines;

			if (mouseX > screenWidth / 2)
				tooltipX = mouseX - 16 - tooltipTextWidth;
			else
				tooltipX = mouseX + 12;
		}

		int tooltipY = mouseY - 12;
		int tooltipHeight = 8;

		if (tooltip.size() > 1) {
			tooltipHeight += (tooltip.size() - 1) * 10;
			if (tooltip.size() > titleLinesCount)
				tooltipHeight += 2; // gap between title lines and next lines
		}

		if (tooltipY + tooltipHeight + 6 > screenHeight)
			tooltipY = screenHeight - tooltipHeight - 6;

		for (int lineNumber = 0; lineNumber < tooltip.size(); ++lineNumber) {
			if (lineNumber + 1 == titleLinesCount)
				tooltipY += 2;

			tooltipY += 10;
		}

		GlStateManager.translate(tooltipX, tooltipY - 50 /* 48px for the map and a 2px gap */, 1);

		double mapScale = 48 / 128d;
		GlStateManager.scale(mapScale, mapScale, 1);
		mc().entityRenderer.getMapItemRenderer().renderMap(mapData, false);

		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.enableRescaleNormal();
	}

}