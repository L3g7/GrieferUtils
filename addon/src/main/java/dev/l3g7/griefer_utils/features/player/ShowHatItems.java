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

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class ShowHatItems extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Items auf dem Kopf anzeigen")
		.description("Zeigt Items, die Spieler im Kopf-Slot haben, über ihnen an.")
		.icon(ItemUtil.createItem(Items.fireworks, 0, true));

	@Mixin(LayerCustomHead.class)
	private static class MixinLayerCustomHead {

		@SuppressWarnings("deprecation")
		@Inject(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;popMatrix()V", shift = At.Shift.BEFORE))
		public void injectDoRenderLayer(EntityLivingBase entity, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale, CallbackInfo ci) {
			if (!FileProvider.getSingleton(ShowHatItems.class).isEnabled())
				return;

			ItemStack itemstack = entity.getCurrentArmor(3);

			if (itemstack.getItem() instanceof ItemBlock || itemstack.getItem() == Items.skull || itemstack.getItem() instanceof ItemArmor)
				return;

			GlStateManager.translate(0.0f, -0.75, 0.275f);
			GlStateManager.scale(0.625f, -0.625f, -0.625f);

			mc().getItemRenderer().renderItem(entity, itemstack, ItemCameraTransforms.TransformType.NONE);
		}

	}

}
