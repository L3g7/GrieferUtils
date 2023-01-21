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

package dev.l3g7.griefer_utils.injection.mixin.furniture;

import dev.l3g7.griefer_utils.features.world.furniture.block.version_specific.VersionBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.pathfinder.WalkNodeProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WalkNodeProcessor.class)
public class MixinWalkNodeProcessor {

	@Inject(method = "func_176170_a", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/Block;getMaterial()Lnet/minecraft/block/material/Material;", ordinal = 3), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	private static void injectWalkNodeProcess(IBlockAccess blockaccessIn, Entity entityIn, int x, int y, int z, int sizeX, int sizeY, int sizeZ, boolean avoidWater, boolean breakDoors, boolean enterDoors, CallbackInfoReturnable<Integer> cir, boolean flag, BlockPos blockpos, BlockPos.MutableBlockPos blockpos$mutableblockpos, int i, int j, int k, Block block) {
		if (block instanceof VersionBlock && ((VersionBlock)block).isFence())
			cir.setReturnValue(-3);
	}

}
