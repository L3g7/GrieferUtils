/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.event.event_bus.Event;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.getPickBlock;

public class BlockPickEvent extends Event {

	public final ItemStack requiredStack;

	public BlockPickEvent(ItemStack requiredStack) {
		this.requiredStack = requiredStack;
	}

	@Mixin(Minecraft.class)
	private static class MixinMinecraft {

		@Shadow
		public MovingObjectPosition objectMouseOver;

		@Shadow
		public WorldClient theWorld;

		@Inject(method = "middleClickMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerCapabilities;isCreativeMode:Z"), cancellable = true)
		private void injectMiddleClickMouse(CallbackInfo ci) {
			MovingObjectPosition mop = objectMouseOver;
			if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
				return;

			Block block = theWorld.getBlockState(mop.getBlockPos()).getBlock();
			if (block.getMaterial() == Material.air)
				return;

			if (new BlockPickEvent(getPickBlock(block, theWorld, mop.getBlockPos())).fire().isCanceled())
				ci.cancel();
		}

	}

}
