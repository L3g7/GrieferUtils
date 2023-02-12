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

package dev.l3g7.griefer_utils.util.render;

import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import net.labymod.settings.elements.ControlElement.IconData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static dev.l3g7.griefer_utils.util.render.GlEngine.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * A utility class for rendering stuff.
 */
public class RenderUtil {

	private static final RenderItem itemRender = mc().getRenderItem();

	/**
	 * Renders an item with a given color tint.
	 */
	public static void renderItem(ItemStack stack, int x, int y, int color) {
		IBakedModel model = itemRender.getItemModelMesher().getItemModel(stack);

		begin();
		disableBlocksBlurAndMipmap();

		// Transform
		Reflection.invoke(itemRender, "setupGuiTransform", x, y, model.isGui3d());
		model = ForgeHooksClient.handleCameraTransforms(model, TransformType.GUI);
		scale(0.5F);
		translate(-0.5F);

		// Draw item
		WorldRenderer worldrenderer = beginWorldDrawing(GL_QUADS, DefaultVertexFormats.ITEM);
		Reflection.invoke(itemRender, "renderQuads", worldrenderer, model.getGeneralQuads(), color, stack);

		finish();
	}

	public static void renderIconData(IconData iconData, int x, int y) {
		if (iconData == null)
			return;

		if (iconData.hasTextureIcon()) {
			textureManager().bindTexture(iconData.getTextureIcon());
			drawUtils().drawTexture(x + 3, y + 3, 256, 256, 16, 16);
		} else if (iconData.hasMaterialIcon())
			drawUtils().drawItem(iconData.getMaterialIcon().createItemStack(), x + 3, y + 2, null);
	}

	public static void drawLine(BlockPos start, BlockPos end, Color color) {
		drawLine(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ(), color);
	}

	/**
	 * Based on <a href="https://github.com/CCBlueX/LiquidBounce/blob/5419a2894b4665b7695d0443180275a70f13607a/src/main/java/net/ccbluex/liquidbounce/utils/render/RenderUtils.java#L82">LiquidBounce's RenderUtils#drawBlockBox</a>
	 */
	public static void drawLine(float startX, float startY, float startZ, float endX, float endY, float endZ, Color color) {
		Entity entity = mc().getRenderViewEntity();

		// Get cam pos
		Vec3d prevPos = new Vec3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ);

		Vec3d cam = prevPos.add(pos(entity).subtract(prevPos).scale(partialTicks()));

		// Update line width
//		float oldLineWidth = glGetFloat(GL_LINE_WIDTH);
		GL11.glLineWidth(1.5f);
		GlStateManager.disableTexture2D();

		// Draw lines
		GlEngine.begin();
		WorldRenderer buf = GlEngine.beginWorldDrawing(GL_LINES, DefaultVertexFormats.POSITION);
		GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

		buf.pos(startX - cam.x, startY - cam.y, startZ - cam.z).endVertex();
		buf.pos(endX - cam.x, endY - cam.y, endZ - cam.z).endVertex();

		GlEngine.finish();

		// Reset line width
//		GL11.glLineWidth(oldLineWidth);
		GlStateManager.enableTexture2D();
	}

}
