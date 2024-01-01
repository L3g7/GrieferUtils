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

package dev.l3g7.griefer_utils.event.events.render;

import dev.l3g7.griefer_utils.core.event_bus.Event;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.reflection.Reflection.c;

public class RenderItemOverlayEvent extends Event {

	public final ItemStack stack;
	public final RenderItem renderItem;
	public final int x;
	public final int y;

	public RenderItemOverlayEvent(RenderItem renderItem, ItemStack stack, int x, int y) {
		this.stack = stack;
		this.renderItem = renderItem;
		this.x = x;
		this.y = y;
	}

	@Mixin(RenderItem.class)
	private static class MixinRenderItem {

		@Inject(method = "renderItemOverlayIntoGUI", at = @At("TAIL"))
		public void injectRenderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
			new RenderItemOverlayEvent(c(this), stack, xPosition, yPosition).fire();
		}

	}

}
