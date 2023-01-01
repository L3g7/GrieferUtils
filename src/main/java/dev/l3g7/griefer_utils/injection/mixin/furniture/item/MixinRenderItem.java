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

package dev.l3g7.griefer_utils.injection.mixin.furniture.item;

import dev.l3g7.griefer_utils.features.world.furniture.block.version_specific.CustomItemBlock;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("deprecation")
@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

	@Shadow
	@Final
	private ItemModelMesher itemModelMesher;
	@Shadow
	@Final
	private TextureManager textureManager;
	@Shadow
	public float zLevel;

	@Shadow
	protected abstract boolean isThereOneNegativeScale(ItemTransformVec3f var1);

	@Shadow
	public abstract void renderItem(ItemStack var1, IBakedModel var2);

	public MixinRenderItem() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Redirect(method = "renderItemModelForEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemModelTransform(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V"))
	public void redirectRenderItemModelTransform(RenderItem renderItem, ItemStack stack, IBakedModel model, ItemCameraTransforms.TransformType cameraTransformType) {
		textureManager.bindTexture(TextureMap.locationBlocksTexture);
		textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
		preTransformModified(stack, cameraTransformType);
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(GL_GREATER, 0.1f);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.pushMatrix();
		ItemCameraTransforms itemcameratransforms = model.getItemCameraTransforms();
		if (stack.getItem() instanceof CustomItemBlock) {
			if (((CustomItemBlock) stack.getItem()).getBlock().getUnlocalizedName().contains("stairs"))
				itemcameratransforms.gui.rotation.y = 45f;

			applyCustomTransform(itemcameratransforms, cameraTransformType);

			if (isThereOneNegativeScale(itemcameratransforms.getTransform(cameraTransformType)))
				GlStateManager.cullFace(GL_FRONT);
		} else {
			model = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(model, cameraTransformType);
		}
		renderItem(stack, model);
		GlStateManager.cullFace(GL_BACK);
		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		textureManager.bindTexture(TextureMap.locationBlocksTexture);
		textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
	}

	@Redirect(method = {"renderItemAndEffectIntoGUI"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemIntoGUI(Lnet/minecraft/item/ItemStack;II)V"))
	private void redirectRenderItemToGui(RenderItem renderItem, ItemStack stack, int x, int y) {
		IBakedModel ibakedmodel = itemModelMesher.getItemModel(stack);
		GlStateManager.pushMatrix();
		textureManager.bindTexture(TextureMap.locationBlocksTexture);
		textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(GL_GREATER, 0.1f);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1f, 1f, 1f, 1f);
		ItemTransformVec3f itemTransformVec3f = ibakedmodel.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GUI);
		setupGuiTransform(x, y, ibakedmodel.isGui3d(), stack.getItem() instanceof CustomItemBlock && itemTransformVec3f != ItemTransformVec3f.DEFAULT);

		if (stack.getItem() instanceof CustomItemBlock) {
			if (((CustomItemBlock) stack.getItem()).getBlock().getUnlocalizedName().contains("stairs"))
				ibakedmodel.getItemCameraTransforms().gui.rotation.y = 45f;

			applyCustomTransform(ibakedmodel.getItemCameraTransforms(), ItemCameraTransforms.TransformType.GUI);
		} else {
			ibakedmodel.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);
		}

		renderItem(stack, ibakedmodel);
		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
		textureManager.bindTexture(TextureMap.locationBlocksTexture);
		textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
	}

	private void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d, boolean isCustomModel) {
		GlStateManager.translate(xPosition, yPosition, 100f + zLevel);
		GlStateManager.translate(8f, 8f, 0f);

		if (isCustomModel) {
			GlStateManager.scale(16f, -16f, 16f);
			GlStateManager.enableLighting();
			return;
		}

		GlStateManager.scale(0.5f, 0.5f, -0.5f);
		if (isGui3d) {
			GlStateManager.scale(40f, 40f, 40f);
			GlStateManager.rotate(210f, 1f, 0f, 0f);
			GlStateManager.rotate(-135f, 0f, 1f, 0f);
			GlStateManager.enableLighting();
		} else {
			GlStateManager.scale(64f, 64f, 64f);
			GlStateManager.rotate(180f, 1f, 0f, 0f);
			GlStateManager.disableLighting();
		}
	}

	private void applyCustomTransform(ItemCameraTransforms itemcameratransforms, ItemCameraTransforms.TransformType cameraTransformType) {
		ItemTransformVec3f itemtransformvec3f = itemcameratransforms.getTransform(cameraTransformType);

		if (itemtransformvec3f == ItemTransformVec3f.DEFAULT) {
			itemcameratransforms.applyTransform(cameraTransformType);
			return;
		}

		GlStateManager.translate(itemtransformvec3f.translation.x, itemtransformvec3f.translation.y, itemtransformvec3f.translation.z);
		GlStateManager.rotate(itemtransformvec3f.rotation.x, 1f, 0f, 0f);
		GlStateManager.rotate(itemtransformvec3f.rotation.y, 0f, 1f, 0f);
		GlStateManager.rotate(itemtransformvec3f.rotation.z, 0f, 0f, 1f);
		GlStateManager.scale(itemtransformvec3f.scale.x, itemtransformvec3f.scale.y, itemtransformvec3f.scale.z);
	}

	public void preTransformModified(ItemStack stack, ItemCameraTransforms.TransformType cameraTransformType) {
		IBakedModel ibakedmodel = itemModelMesher.getItemModel(stack);
		Item item = stack.getItem();
		if (item == null)
			return;

		if (!ibakedmodel.isGui3d())
			GlStateManager.scale(2f, 2f, 2f);
		GlStateManager.color(1f, 1f, 1f, 1f);

		if (!(item instanceof CustomItemBlock))
			return;

		if (cameraTransformType.equals(ItemCameraTransforms.TransformType.FIRST_PERSON)) {
			GlStateManager.rotate(-45f, 0f, 1f, 0f);
			GlStateManager.scale(1.25f, 1.25f, 1.25f);
		} else if (cameraTransformType.equals(ItemCameraTransforms.TransformType.THIRD_PERSON)) {
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			GlStateManager.translate(0.0625f, -0.4375f, -0.0625f);
			GlStateManager.rotate(-90f, 1f, 0f, 0f);
			GlStateManager.rotate(180f, 0f, 1f, 0f);
			GlStateManager.translate(0.0625, 0.125, -0.625);
		}
	}

}