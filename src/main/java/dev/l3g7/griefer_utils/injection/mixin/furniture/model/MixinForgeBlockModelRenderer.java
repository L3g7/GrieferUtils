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

package dev.l3g7.griefer_utils.injection.mixin.furniture.model;

import dev.l3g7.griefer_utils.features.world.furniture.abstraction.BlockBakedModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ForgeBlockModelRenderer.class)
public class MixinForgeBlockModelRenderer {

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/IBakedModel;getFaceQuads(Lnet/minecraft/util/EnumFacing;)Ljava/util/List;"))
	private static List<BakedQuad> redirectRender(IBakedModel iBakedModel, EnumFacing facing, VertexLighterFlat lighter, IBlockAccess world, IBakedModel model, Block block, BlockPos pos, WorldRenderer wr, boolean checkSides) {
		if (iBakedModel instanceof BlockBakedModel)
			return ((BlockBakedModel) iBakedModel).getQuads(block.getActualState(world.getBlockState(pos), world, pos), facing);

		return iBakedModel.getFaceQuads(facing);
	}

}
