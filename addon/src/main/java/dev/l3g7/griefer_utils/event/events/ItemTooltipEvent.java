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

package dev.l3g7.griefer_utils.event.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

public class ItemTooltipEvent extends Event {

	public final boolean showAdvancedItemTooltips;
	public final ItemStack itemStack;
	public final List<String> toolTip;

	public ItemTooltipEvent(Object itemStack, List<String> toolTip, boolean showAdvancedItemTooltips) {
		this.itemStack = (ItemStack) itemStack;
		this.toolTip = toolTip;
		this.showAdvancedItemTooltips = showAdvancedItemTooltips;
	}

	@Mixin(ItemStack.class)
	private static class MixinItemStack  {

		@Inject(method = "getTooltip", at = @At("RETURN"))
		public void injectGetTooltip(EntityPlayer playerIn, boolean advanced, CallbackInfoReturnable<List<String>> cir) {
			MinecraftForge.EVENT_BUS.post(new ItemTooltipEvent(this, cir.getReturnValue(), advanced));
		}

	}

}