/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.features.player;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class ShowHatItems extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Items auf dem Kopf anzeigen")
		.description("Zeigt Items, die Spieler im Kopf-Slot haben, über ihnen an.")
		.icon(ItemUtil.createItem(Items.fireworks, 0, true));

	@Mixin(LayerCustomHead.class)
	private static class MixinLayerCustomHead {

		@SuppressWarnings("deprecation")
		@Inject(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", shift = At.Shift.AFTER), cancellable = true)
		public void injectDoRenderLayer(EntityLivingBase entity, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale, CallbackInfo ci) {
			if (!FileProvider.getSingleton(ShowHatItems.class).isEnabled() || entity instanceof EntityArmorStand)
				return;

			ItemStack stack = entity.getCurrentArmor(3);

			if (stack.getItem() == Items.skull || stack.getItem() instanceof ItemArmor)
				return;

			if (stack.getItem() instanceof ItemBlock) {
				IBakedModel model = mc().getRenderItem().getItemModelMesher().getItemModel(stack);
				if (model.isGui3d())
					return;
			}

			GlStateManager.translate(0.0f, -0.75, 0.275f);
			GlStateManager.scale(0.625f, -0.625f, -0.625f);

			mc().getItemRenderer().renderItem(entity, stack, ItemCameraTransforms.TransformType.NONE);
			GlStateManager.popMatrix();
			ci.cancel();
		}

	}

}
