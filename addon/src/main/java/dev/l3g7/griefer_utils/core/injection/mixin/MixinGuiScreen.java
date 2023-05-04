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

package dev.l3g7.griefer_utils.core.injection.mixin;

import dev.l3g7.griefer_utils.event.events.GuiInitEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderToolTipEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {

	@Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
	public void injectRenderTooltip(ItemStack stack, int x, int y, CallbackInfo ci) {
		if (MinecraftForge.EVENT_BUS.post(new RenderToolTipEvent(stack, (GuiScreen) (Object) this, x, y)))
			ci.cancel();
	}

	@Inject(method = "initGui", at = @At("HEAD"))
	public void injectInitGui(CallbackInfo ci) {
		MinecraftForge.EVENT_BUS.post(new GuiInitEvent((GuiScreen) (Object) this));
	}

}
