package dev.l3g7.griefer_utils.core.util.render;

import dev.l3g7.griefer_utils.core.events.render.RenderToolTipEvent;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * A utility class for rendering tooltips.
 */
public class TooltipRenderUtil {

	public static void renderToolTipWithLeftPadding(RenderToolTipEvent event, float padding, BiConsumer<Float, Float> paddingContentRenderer) {
		List<String> textLines = event.stack.getTooltip(player(), settings().advancedItemTooltips);
		for (int i = 0; i < textLines.size(); ++i)
			textLines.set(i, (i == 0 ? event.stack.getRarity().rarityColor : "§7") + textLines.get(i));

		if (textLines.isEmpty())
			return;

		event.cancel();
		renderToolTipWithPadding(textLines, event.x, event.y, event.screen.width, event.screen.height, padding, 0, padding, paddingContentRenderer);
	}

	public static void renderToolTipWithPadding(List<String> textLines, int mouseX, int mouseY, int width, int height, float leftPadding, float otherPadding, float minHeight, BiConsumer<Float, Float> paddingContentRenderer) {
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();

		int maxLineWidth = 0;
		for (String line : textLines) {
			int lineWidth = DrawUtils.getStringWidth(line);
			if (lineWidth > maxLineWidth)
				maxLineWidth = lineWidth;
		}

		float mouseOffset = leftPadding + 12;
		float drawX = mouseX + mouseOffset;
		float drawY = mouseY - 12;
		float defaultHeight = textLines.size() == 1 ? 14.5f : 8;
		if (textLines.size() > 1)
			defaultHeight += 2 + (textLines.size() - 1) * 10;
		defaultHeight = Math.max(minHeight, defaultHeight);

		if (drawX + maxLineWidth > width)
			drawX -= mouseOffset + maxLineWidth;
		if (drawY + defaultHeight + 6 > height)
			drawY = height - defaultHeight - 6;

		int color = 0xF0100010;
		float y = drawY + defaultHeight + 3 + otherPadding * 2;
		float x = drawX + maxLineWidth + 3 + otherPadding;
		float paddedX = drawX - 5 - leftPadding;
		drawY -= 3;
		drawGradientRect(paddedX, drawY - 1, x, drawY, 0, color, color);
		drawGradientRect(paddedX, y, x, y + 1, 0, color, color);
		drawGradientRect(drawX - 3, drawY, x, y, 0, color, color);
		drawGradientRect(x, drawY, x + 1, y, 0, color, color);
		drawGradientRect(paddedX - 1, drawY, drawX - 3, y, 0, color, color);

		int startColor = 0x505000FF;
		int endColor = (startColor & 0xFEFEFE) >> 1 | (startColor & 0xFF000000);
		drawGradientRect(paddedX, drawY + 1, paddedX + 1, y - 1, 0, startColor, endColor);
		drawGradientRect(x - 1, drawY + 1, x, y - 1, 0, startColor, endColor);
		drawGradientRect(paddedX, drawY, x, drawY + 1, 0, startColor, startColor);
		drawGradientRect(paddedX, y - 1, x, y, 0, endColor, endColor);
		drawY += 3 + otherPadding;

		float rectTop = drawY;
		for (int i = 0; i < textLines.size(); ++i) {
			String line = textLines.get(i);
			DrawUtils.drawStringWithShadow(line, drawX, drawY, -1);
			if (i == 0)
				drawY += 2;
			drawY += 10;
		}

		GlStateManager.enableBlend();
		GlStateManager.enableDepth();
		GlStateManager.enableRescaleNormal();
		GlStateManager.pushMatrix();
		paddingContentRenderer.accept(paddedX + 2, rectTop);
		GlStateManager.popMatrix();
	}

	public static Pair<Integer, Integer> getTooltipTranslation(List<String> textLines, int mouseX, int mouseY, int width, int height) {
		if (textLines.isEmpty())
			throw new IllegalArgumentException("textLines can't be empty!");

		int tooltipTextWidth = 0;

		for (String textLine : textLines) {
			int textLineWidth = mc().fontRendererObj.getStringWidth(textLine);

			if (textLineWidth > tooltipTextWidth) {
				tooltipTextWidth = textLineWidth;
			}
		}

		boolean needsWrap = false;

		int titleLinesCount = 1;
		int tooltipX = mouseX + 12;
		if (tooltipX + tooltipTextWidth + 4 > width) {
			tooltipX = mouseX - 16 - tooltipTextWidth;
			if (tooltipX < 4) { // if the tooltip doesn't fit on the screen
				if (mouseX > width / 2)
					tooltipTextWidth = mouseX - 12 - 8;
				else
					tooltipTextWidth = width - 16 - mouseX;
				needsWrap = true;
			}
		}

		if (needsWrap) {
			int wrappedTooltipWidth = 0;
			List<String> wrappedTextLines = new ArrayList<>();
			for (int i = 0; i < textLines.size(); i++) {
				String textLine = textLines.get(i);
				List<String> wrappedLine = mc().fontRendererObj.listFormattedStringToWidth(textLine, tooltipTextWidth);
				if (i == 0) {
					titleLinesCount = wrappedLine.size();
				}

				for (String line : wrappedLine) {
					int lineWidth = mc().fontRendererObj.getStringWidth(line);
					if (lineWidth > wrappedTooltipWidth) {
						wrappedTooltipWidth = lineWidth;
					}
					wrappedTextLines.add(line);
				}
			}
			tooltipTextWidth = wrappedTooltipWidth;
			textLines = wrappedTextLines;

			if (mouseX > width / 2)
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

		if (tooltipY + tooltipHeight + 6 > height)
			tooltipY = height - tooltipHeight - 6;

		return Pair.of(tooltipX, tooltipY);
	}

	public static void drawGradientRect(double left, double top, double right, double bottom, double zLevel, int startColor, int endColor) {
		float startA = (startColor >> 24 & 255) / 255f;
		float startR = (startColor >> 16 & 255) / 255f;
		float startG = (startColor >> 8 & 255) / 255f;
		float startB = (startColor & 255) / 255f;
		float endA = (endColor >> 24 & 255) / 255f;
		float endR = (endColor >> 16 & 255) / 255f;
		float endG = (endColor >> 8 & 255) / 255f;
		float endB = (endColor & 255) / 255f;

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.shadeModel(GL_SMOOTH);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos(right, top, zLevel).color(startR, startG, startB, startA).endVertex();
		worldrenderer.pos(left, top, zLevel).color(startR, startG, startB, startA).endVertex();
		worldrenderer.pos(left, bottom, zLevel).color(endR, endG, endB, endA).endVertex();
		worldrenderer.pos(right, bottom, zLevel).color(endR, endG, endB, endA).endVertex();
		tessellator.draw();

		GlStateManager.shadeModel(GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}
}
