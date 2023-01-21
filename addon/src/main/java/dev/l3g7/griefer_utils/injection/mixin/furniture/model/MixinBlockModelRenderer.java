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

package dev.l3g7.griefer_utils.injection.mixin.furniture.model;

import dev.l3g7.griefer_utils.features.world.furniture.abstraction.BlockBakedModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BlockModelRenderer.class)
public abstract class MixinBlockModelRenderer {

	@Shadow
	protected abstract void renderModelBrightnessColorQuads(float brightness, float red, float green, float blue, List<BakedQuad> listQuads);

	@Redirect(method = "renderModelAmbientOcclusion", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/IBakedModel;getFaceQuads(Lnet/minecraft/util/EnumFacing;)Ljava/util/List;"))
	public List<BakedQuad> redirectAmbientOcclusionQuads(IBakedModel iBakedModel, EnumFacing enumFacing, IBlockAccess blockAccessIn, IBakedModel modelIn, Block blockIn, BlockPos blockPosIn, WorldRenderer worldRendererIn, boolean checkSides) {
		if (iBakedModel instanceof BlockBakedModel)
			return ((BlockBakedModel) iBakedModel).getQuads(blockIn.getActualState(blockAccessIn.getBlockState(blockPosIn), blockAccessIn, blockPosIn), enumFacing);

		return iBakedModel.getFaceQuads(enumFacing);
	}

	@Redirect(method = "renderModelStandard", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/IBakedModel;getFaceQuads(Lnet/minecraft/util/EnumFacing;)Ljava/util/List;"))
	public List<BakedQuad> redirectStandardQuads(IBakedModel iBakedModel, EnumFacing enumFacing, IBlockAccess blockAccessIn, IBakedModel modelIn, Block blockIn, BlockPos blockPosIn, WorldRenderer worldRendererIn, boolean checkSides) {
		if (iBakedModel instanceof BlockBakedModel) {
			return ((BlockBakedModel)iBakedModel).getQuads(blockIn.getActualState(blockAccessIn.getBlockState(blockPosIn), blockAccessIn, blockPosIn), enumFacing);
		}
		return iBakedModel.getFaceQuads(enumFacing);
	}

	@Inject(method = "renderModelBrightnessColor", at = @At("HEAD"), cancellable = true)
	public void injectRenderModelBrightnessColor(IBakedModel bakedModel, float p_178262_2_, float red, float green, float blue, CallbackInfo callbackInfo) {
		for (EnumFacing facing : EnumFacing.values()) {
			List<BakedQuad> quads = bakedModel instanceof BlockBakedModel
				? ((BlockBakedModel) bakedModel).getQuads(null, facing)
				: bakedModel.getFaceQuads(facing);

			renderModelBrightnessColorQuads(p_178262_2_, red, green, blue, quads);
		}

		renderModelBrightnessColorQuads(p_178262_2_, red, green, blue, bakedModel.getGeneralQuads());
		callbackInfo.cancel();
	}

}

