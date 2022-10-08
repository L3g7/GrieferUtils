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

package dev.l3g7.griefer_utils.util.render;

import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.StaticImport.mc;
import static dev.l3g7.griefer_utils.util.render.GlEngine.*;
import static org.lwjgl.opengl.GL11.GL_QUADS;

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
		RenderItem itemRender = mc().getRenderItem();

		begin();
		disableBlocksBlurAndMipmap();

		// Transform
		Reflection.invoke(itemRender, "setupGuiTransform", x, y, model.isGui3d());
		model = ForgeHooksClient.handleCameraTransforms(model, TransformType.GUI);
		scale(0.5F);
		translate(-0.5F, -0.5F, -0.5F);

		// Draw item
		WorldRenderer worldrenderer = beginWorldDrawing(GL_QUADS, DefaultVertexFormats.ITEM);
		Reflection.invoke(itemRender, "renderQuads", worldrenderer, model.getGeneralQuads(), color, stack);

		end();
	}

}
