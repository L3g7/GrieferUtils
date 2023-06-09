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

package dev.l3g7.griefer_utils.mixin;

import dev.l3g7.griefer_utils.event.events.render.RenderItemOverlayEvent;
import dev.l3g7.griefer_utils.features.render.SkullEnchantmentFix;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

	@Shadow
	protected abstract void draw(WorldRenderer renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha);

	@Inject(method = "renderItemOverlayIntoGUI", at = @At("TAIL"))
	public void injectRenderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
		MinecraftForge.EVENT_BUS.post(new RenderItemOverlayEvent((RenderItem) (Object) this, stack, xPosition, yPosition));
	}

	@Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityItemStackRenderer;renderByItem(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
	public void injectRenderItem(ItemStack stack, IBakedModel model, CallbackInfo ci) {
		SkullEnchantmentFix.renderEffect(stack);
	}

	@Inject(method = "renderEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;disableLighting()V"))
	public void injectRenderEffect(IBakedModel model, CallbackInfo ci) {
		SkullEnchantmentFix.setDepthFunc(model);
	}

}
