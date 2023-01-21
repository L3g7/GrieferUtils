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

import com.google.common.base.Charsets;
import dev.l3g7.griefer_utils.features.world.furniture.abstraction.ModModelBakery;
import dev.l3g7.griefer_utils.features.world.furniture.abstraction.ModModelBlockDefinition;
import dev.l3g7.griefer_utils.features.world.furniture.multipart.Multipart;
import dev.l3g7.griefer_utils.features.world.furniture.multipart.MultipartBakedModel;
import dev.l3g7.griefer_utils.features.world.furniture.multipart.Selector;
import dev.l3g7.griefer_utils.features.world.furniture.multipart.VariantList;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.model.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.RegistrySimple;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Mixin(ModelBakery.class)
public abstract class MixinModelBakery implements ModModelBakery {

	@Shadow
	@Final
	private Map<ResourceLocation, ModelBlock> models;
	@Shadow
	@Final
	private static Logger LOGGER;
	@Shadow
	protected RegistrySimple<ModelResourceLocation, IBakedModel> bakedRegistry;
	@Shadow
	@Final
	protected BlockModelShapes blockModelShapes;
	@Shadow
	@Final
	protected IResourceManager resourceManager;
	@Shadow
	@Final
	protected Map<ResourceLocation, TextureAtlasSprite> sprites;
	@Shadow
	@Final
	protected TextureMap textureMap;
	private final Map<ModelBlockDefinition, Collection<ModelResourceLocation>> multipartVariantMap = new LinkedHashMap<>();
	private Map<IBlockState, ModelResourceLocation> stateModelLocations;

	@Shadow
	protected abstract IBakedModel bakeModel(ModelBlock modelBlockIn, ModelRotation modelRotationIn, boolean uvLocked);

	@Shadow
	protected abstract void registerVariant(ModelBlockDefinition p_177569_1_, ModelResourceLocation p_177569_2_);

	@Shadow
	protected abstract Set<ResourceLocation> getTextureLocations(ModelBlock p_177585_1_);

	@Inject(method = "bakeModel(Lnet/minecraft/client/renderer/block/model/ModelBlock;Lnet/minecraft/client/resources/model/ModelRotation;Z)Lnet/minecraft/client/resources/model/IBakedModel;", at = @At("HEAD"))
	public void injectBakeModelHead(ModelBlock modelBlockIn, ModelRotation modelRotationIn, boolean uvLocked, CallbackInfoReturnable<IBakedModel> cir) {
		addSprite(modelBlockIn.resolveTextureName("particle"));

		for (BlockPart blockpart : modelBlockIn.getElements())
			for (EnumFacing enumfacing : blockpart.mapFaces.keySet())
				addSprite(modelBlockIn.resolveTextureName(blockpart.mapFaces.get(enumfacing).texture));
	}

	private void addSprite(String resourceName) {
		ResourceLocation resourceLocation = new ResourceLocation(resourceName);
		sprites.put(resourceLocation, textureMap.getAtlasSprite(resourceLocation.toString()));
	}

	@Redirect(method = "loadVariants", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;registerVariant(Lnet/minecraft/client/renderer/block/model/ModelBlockDefinition;Lnet/minecraft/client/resources/model/ModelResourceLocation;)V"))
	public void redirectRegisterVariant(ModelBakery modelBakery, ModelBlockDefinition mcModelBlockDefinition, ModelResourceLocation modelResourceLocation) {
		if (stateModelLocations == null)
			stateModelLocations = blockModelShapes.getBlockStateMapper().putAllStateModelLocations();

		ModModelBlockDefinition modelBlockDefinition = (ModModelBlockDefinition) mcModelBlockDefinition;
		if (!modelBlockDefinition.hasMultipartData()) {
			registerVariant(mcModelBlockDefinition, modelResourceLocation);
			return;
		}

		IBlockState blockState = stateModelLocations.entrySet().stream().filter(entry -> entry.getValue().equals(modelResourceLocation)).map(Map.Entry::getKey).findFirst().orElse(null);
		if (blockState == null) {
			System.err.println("Couldn't find blockstate by model resource location: " + modelResourceLocation.toString());
			return;
		}

		modelBlockDefinition.getMultipartData().setStateContainer(blockState.getBlock().getBlockState());
		Collection<ModelResourceLocation> multipartVariants = multipartVariantMap.computeIfAbsent(mcModelBlockDefinition, k -> new ArrayList<>());
		multipartVariants.add(modelResourceLocation);


		for (VariantList variantlist : modelBlockDefinition.getMultipartVariants())
			loadVariantList(modelResourceLocation, variantlist);
	}

	@Override
	public void bakeMultipartModels() {
		for (Map.Entry<ModelBlockDefinition, Collection<ModelResourceLocation>> entry : multipartVariantMap.entrySet()) {
			ModelBlockDefinition modelblockdefinition = entry.getKey();
			Multipart multipart = ((ModModelBlockDefinition) modelblockdefinition).getMultipartData();
			String s = Block.blockRegistry.getNameForObject(multipart.getStateContainer().getBlock()).toString();
			MultipartBakedModel.Builder builder = new MultipartBakedModel.Builder();

			for (Selector selector : multipart.getSelectors()) {
				IBakedModel ibakedmodel1 = createRandomModelForVariantList(selector.getVariantList().getVariantList(), "selector of " + s);
				if (ibakedmodel1 != null)
					builder.putModel(selector.getPredicate(multipart.getStateContainer()), ibakedmodel1);
			}

			IBakedModel ibakedmodel2 = builder.makeMultipartModel();
			for (ModelResourceLocation modelresourcelocation1 : entry.getValue()) {
				try {
					modelblockdefinition.getVariants(modelresourcelocation1.getVariant());
				} catch (Exception ignored) {
					bakedRegistry.putObject(modelresourcelocation1, ibakedmodel2);
				}
			}
		}
	}

	private void loadVariantList(ModelResourceLocation modelResourceLocation, VariantList variantList) {
		for (Variant variant : variantList.getVariantList()) {
			ResourceLocation resourcelocation = variant.getModelLocation();

			if (models.get(resourcelocation) != null)
				continue;

			try {
				ModelBlock loadedModel = loadModelOwn(resourcelocation);
				models.put(resourcelocation, loadedModel);
				if (!(((Object) this) instanceof net.minecraftforge.client.model.ModelLoader))
					continue;

				Set<ResourceLocation> textures = Reflection.get(this, "textures");
				textures.addAll(getTextureLocations(loadedModel));
			} catch (Exception exception) {
				MixinModelBakery.LOGGER.warn("Unable to load block model: '{}' for variant: '{}': {} ", resourcelocation, modelResourceLocation, exception);
			}
		}
	}

	private ModelBlock loadModelOwn(ResourceLocation location) throws IOException {
		IResource iresource = resourceManager.getResource(new ResourceLocation(location.getResourceDomain(), "models/" + location.getResourcePath() + ".json"));
		ModelBlock modelblock;

		try (InputStreamReader reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8)) {
			modelblock = ModelBlock.deserialize(reader);
			modelblock.name = location.toString();
		}

		if (modelblock.getParentLocation() != null) {
			models.put(modelblock.getParentLocation(), loadModelOwn(modelblock.getParentLocation()));
			modelblock.getParentFromMap(models);
		}

		return modelblock;
	}

	private IBakedModel createRandomModelForVariantList(List<Variant> variantsIn, String modelLocation) {
		if (variantsIn.isEmpty())
			return null;

		WeightedBakedModel.Builder builder = new WeightedBakedModel.Builder();
		int i = 0;

		for (Variant variant : variantsIn) {
			ModelBlock modelblock = models.get(variant.getModelLocation());

			if (modelblock == null || !modelblock.isResolved()) {
				MixinModelBakery.LOGGER.warn("Missing model for: {}", modelLocation);
				continue;
			}

			if (modelblock.getElements().isEmpty()) {
				MixinModelBakery.LOGGER.warn("Missing elements for: {}", modelLocation);
				continue;
			}

			@SuppressWarnings("deprecation")
			IBakedModel ibakedmodel = bakeModel(modelblock, variant.getRotation(), variant.isUvLocked());
			if (ibakedmodel == null)
				continue;

			builder.add(ibakedmodel, variant.getWeight());
			i++;
		}

		if (i == 0)
			MixinModelBakery.LOGGER.warn("No weighted models for: {}", modelLocation);
		else if (i == 1)
			return builder.first();

		return builder.build();
	}

}
