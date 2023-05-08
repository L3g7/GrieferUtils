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

package dev.l3g7.griefer_utils.features.render;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class SkullEnchantmentFix extends Feature {

	private static final ItemStack ICON = ItemUtil.createItem(Items.skull, 0, true);

	@SuppressWarnings("unchecked, deprecation")
	private static final IBakedModel cubeModel = new IBakedModel() {
		private final List<BakedQuad>[] bakedQuads = new List[] { // Data was copied from the IBakedModel of a standard dirt block
			ImmutableList.of(new BakedQuad(new int[] {0, 0, 1065353216, -8421505, 1048576655, 1044383007, 33024, 0, 0, 0, -8421505, 1048576655, 1046477537, 33024, 1065353216, 0, 0, -8421505, 1049623921, 1046477537, 33024, 1065353216, 0, 1065353216, -8421505, 1049623921, 1044383007, 33024}, -1, EnumFacing.DOWN)),
			ImmutableList.of(new BakedQuad(new int[] {0, 1065353216, 0, -1, 1048576655, 1044383007, 32512, 0, 1065353216, 1065353216, -1, 1048576655, 1046477537, 32512, 1065353216, 1065353216, 1065353216, -1, 1049623921, 1046477537, 32512, 1065353216, 1065353216, 0, -1, 1049623921, 1044383007, 32512}, -1, EnumFacing.UP)),
			ImmutableList.of(new BakedQuad(new int[] {1065353216, 1065353216, 0, -3355444, 1048576655, 1044383007, 8454144, 1065353216, 0, 0, -3355444, 1048576655, 1046477537, 8454144, 0, 0, 0, -3355444, 1049623921, 1046477537, 8454144, 0, 1065353216, 0, -3355444, 1049623921, 1044383007, 8454144}, -1, EnumFacing.NORTH)),
			ImmutableList.of(new BakedQuad(new int[] {0, 1065353216, 1065353216, -3355444, 1048576655, 1044383007, 8323072, 0, 0, 1065353216, -3355444, 1048576655, 1046477537, 8323072, 1065353216, 0, 1065353216, -3355444, 1049623921, 1046477537, 8323072, 1065353216, 1065353216, 1065353216, -3355444, 1049623921, 1044383007, 8323072}, -1, EnumFacing.SOUTH)),
			ImmutableList.of(new BakedQuad(new int[] {0, 1065353216, 0, -6710887, 1048576655, 1044383007, 129, 0, 0, 0, -6710887, 1048576655, 1046477537, 129, 0, 0, 1065353216, -6710887, 1049623921, 1046477537, 129, 0, 1065353216, 1065353216, -6710887, 1049623921, 1044383007, 129}, -1, EnumFacing.WEST)),
			ImmutableList.of(new BakedQuad(new int[] {1065353216, 1065353216, 1065353216, -6710887, 1048576655, 1044383007, 127, 1065353216, 0, 1065353216, -6710887, 1048576655, 1046477537, 127, 1065353216, 0, 0, -6710887, 1049623921, 1046477537, 127, 1065353216, 1065353216, 0, -6710887, 1049623921, 1044383007, 127}, -1, EnumFacing.EAST))
		};

		public List<BakedQuad> getFaceQuads(EnumFacing facing) {
			return bakedQuads[facing.getIndex()];
		}

		public List<BakedQuad> getGeneralQuads() { return new ArrayList<>(); }
		public boolean isAmbientOcclusion() { return false; }
		public boolean isGui3d() { return false; }
		public boolean isBuiltInRenderer() { return false; }
		public TextureAtlasSprite getParticleTexture() { return null; }
		public ItemCameraTransforms getItemCameraTransforms() { return null; }
	};

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Kopf-Verzauberung fixen")
		.description("Behebt, dass Verzauberungen von Köpfe nicht angezeigt werden.")
		.icon(ICON);

	public static void setDepthFunc(IBakedModel ibakedModel) {
		if (ibakedModel == cubeModel)
			GlStateManager.depthFunc(GL11.GL_ALWAYS);
	}

	public static void renderEffect(ItemStack stack) {
		if (stack.getItem() != Items.skull)
			return;

		if (!FileProvider.getSingleton(SkullEnchantmentFix.class).isEnabled() && stack != ICON)
			return;

		if (stack.hasEffect())
			Reflection.invoke(mc().getRenderItem(), "renderEffect", cubeModel);
	}

}
