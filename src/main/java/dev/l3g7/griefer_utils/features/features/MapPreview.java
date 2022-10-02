package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import joptsimple.internal.Strings;
import net.labymod.settings.elements.SettingsElement;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class MapPreview extends Feature {

	private List<String> tooltip = new ArrayList<>();

	private final BooleanSetting enabled = new BooleanSetting()
		.name("Karten Vorschau")
		.config("features.map_preview.active")
		.description("Zeigt in der Beschreibung von Karten eine Vorschau an.")
		.icon(Material.MAP)
		.defaultValue(false);

	public MapPreview() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onTooltip(ItemTooltipEvent event) {
		if (!isActive() || !isOnGrieferGames() || !(event.itemStack.getItem() instanceof ItemMap))
			return;

		tooltip = event.toolTip;

		// Add space(s)
		int lines = (int) Math.ceil(48 / (mc().fontRendererObj.FONT_HEIGHT + 1d));
		int spaces = (int) Math.ceil(48 / (double) mc().fontRendererObj.getCharWidth(' '));

		for (int i = 0; i < lines; i++)
			event.toolTip.add(Strings.repeat(' ', spaces));
	}

	@SubscribeEvent
	public void onRender(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (!isActive() || !isOnGrieferGames() || !(event.gui instanceof GuiContainer))
			return;

		GuiContainer currentScreen = (GuiContainer) event.gui;
		Slot slot = currentScreen.getSlotUnderMouse();
		if (slot == null || !slot.getHasStack())
			return;

		ItemStack stack = slot.getStack();
		if (!(stack.getItem() instanceof ItemMap))
			return;

		MapData mapData = Items.filled_map.getMapData(stack, world());
		if (mapData == null)
			return;

		EntityRenderer renderer = mc().entityRenderer;

		// Enable lighting (otherwise it's darker than it should be)
		renderer.enableLightmap();
		renderMap(tooltip, event.mouseX, event.mouseY, currentScreen.width, currentScreen.height, mc().fontRendererObj, mapData);
		renderer.disableLightmap();
	}

	/**
	 * Source of the code to calculate the position: {@link net.minecraftforge.fml.client.config.GuiUtils#drawHoveringText(List, int, int, int, int, int, FontRenderer) GuiUtils.drawHoveringText}
	 */
	private void renderMap(List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, FontRenderer font, MapData mapData) {
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		int tooltipTextWidth = 0;

		for (String textLine : textLines) {
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
			for (int i = 0; i < textLines.size(); i++) {
				String textLine = textLines.get(i);
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
			textLines = wrappedTextLines;

			if (mouseX > screenWidth / 2)
				tooltipX = mouseX - 16 - tooltipTextWidth;
			else
				tooltipX = mouseX + 12;
		}

		int tooltipY = mouseY - 12;
		int tooltipHeight = 8;

		if (textLines.size() > 1) {
			tooltipHeight += (textLines.size() - 1) * 10;
			if (textLines.size() > titleLinesCount)
				tooltipHeight += 2; // gap between title lines and next lines
		}

		if (tooltipY + tooltipHeight + 6 > screenHeight)
			tooltipY = screenHeight - tooltipHeight - 6;

		for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
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
