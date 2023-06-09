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

import dev.l3g7.griefer_utils.event.events.BlockPickEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeHooks.class)
public class MixinForgeHooks {

	@Inject(method = "onPickBlock", at = @At("HEAD"), remap = false, cancellable = true)
	private static void inject(MovingObjectPosition target, EntityPlayer player, World world, CallbackInfoReturnable<Boolean> cir) {
		if (target == null || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
			return;

		Block block = world.getBlockState(target.getBlockPos()).getBlock();
		if (block.isAir(world, target.getBlockPos()))
			return;

		if (MinecraftForge.EVENT_BUS.post(new BlockPickEvent(block.getPickBlock(target, world, target.getBlockPos(), player))))
			cir.setReturnValue(true);
	}

}
