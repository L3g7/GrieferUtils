/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events;

import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
			new ItemTooltipEvent(this, cir.getReturnValue(), advanced).fire();
		}

	}

}