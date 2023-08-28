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

import dev.l3g7.griefer_utils.core.event_bus.Event;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

		@Shadow
		public EntityPlayerSP thePlayer;

		@Inject(method = "middleClickMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerCapabilities;isCreativeMode:Z"), cancellable = true)
		private void injectMiddleClickMouse(CallbackInfo ci) {
			MovingObjectPosition mop = objectMouseOver;
			if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
				return;

			Block block = theWorld.getBlockState(mop.getBlockPos()).getBlock();
			if (block.isAir(theWorld, mop.getBlockPos()))
				return;

			if (new BlockPickEvent(block.getPickBlock(mop, theWorld, mop.getBlockPos(), thePlayer)).fire().isCanceled())
				ci.cancel();
		}

	}

	@Mixin(ForgeHooks.class)
	public static class MixinForgeHooks {

		@Inject(method = "onPickBlock", at = @At("HEAD"), remap = false, cancellable = true)
		private static void inject(MovingObjectPosition target, EntityPlayer player, World world, CallbackInfoReturnable<Boolean> cir) {
			if (target == null || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
				return;

			Block block = world.getBlockState(target.getBlockPos()).getBlock();
			if (block.isAir(world, target.getBlockPos()))
				return;

			if (new BlockPickEvent(block.getPickBlock(target, world, target.getBlockPos(), player)).fire().isCanceled())
				cir.setReturnValue(true);
		}

	}

}
