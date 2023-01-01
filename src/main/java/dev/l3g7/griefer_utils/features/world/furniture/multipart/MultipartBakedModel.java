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

package dev.l3g7.griefer_utils.features.world.furniture.multipart;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.l3g7.griefer_utils.features.world.furniture.abstraction.BlockBakedModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class MultipartBakedModel implements BlockBakedModel {

	private final Map<Predicate<IBlockState>, IBakedModel> selectors;
	protected final boolean ambientOcclusion;
	protected final boolean gui3D;
	protected final TextureAtlasSprite particleTexture;
	protected final ItemCameraTransforms cameraTransforms;

	public MultipartBakedModel(Map<Predicate<IBlockState>, IBakedModel> selectorsIn) {
		selectors = selectorsIn;
		IBakedModel ibakedmodel = selectorsIn.values().iterator().next();
		ambientOcclusion = ibakedmodel.isAmbientOcclusion();
		gui3D = ibakedmodel.isGui3d();
		particleTexture = ibakedmodel.getParticleTexture();
		cameraTransforms = ibakedmodel.getItemCameraTransforms();
	}

	public List<BakedQuad> getFaceQuads(EnumFacing enumFacing) {
		return Collections.emptyList();
	}

	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side) {
		ArrayList<BakedQuad> list = Lists.newArrayList();
		if (state != null) {
			for (Map.Entry<Predicate<IBlockState>, IBakedModel> entry : selectors.entrySet()) {
				if (!entry.getKey().test(state)) continue;
				list.addAll(entry.getValue().getGeneralQuads());
				list.addAll(entry.getValue().getFaceQuads(side));
			}
		}
		return list;
	}

	public List<BakedQuad> getGeneralQuads() {
		return Collections.emptyList();
	}

	public boolean isAmbientOcclusion() {
		return this.ambientOcclusion;
	}

	public boolean isGui3d() {
		return this.gui3D;
	}

	public boolean isBuiltInRenderer() {
		return false;
	}

	public TextureAtlasSprite getParticleTexture() {
		return this.particleTexture;
	}

	public ItemCameraTransforms getItemCameraTransforms() {
		return this.cameraTransforms;
	}

	public static class Builder {

		public final Map<Predicate<IBlockState>, IBakedModel> builderSelectors = Maps.newLinkedHashMap();

		public void putModel(Predicate<IBlockState> predicate, IBakedModel model) {
			this.builderSelectors.put(predicate, model);
		}

		public IBakedModel makeMultipartModel() {
			return new MultipartBakedModel(this.builderSelectors);
		}
	}

}
