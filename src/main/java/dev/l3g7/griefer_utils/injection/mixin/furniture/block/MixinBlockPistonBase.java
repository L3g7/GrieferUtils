/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.injection.mixin.furniture.block;

import dev.l3g7.griefer_utils.features.world.furniture.block.version_specific.VersionBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPistonBase.class)
public class MixinBlockPistonBase {

	@Inject(method = "canPush", at = @At("HEAD"), cancellable = true)
	private static void injectCanPush(Block blockIn, World worldIn, BlockPos pos, EnumFacing direction, boolean allowDestroy, CallbackInfoReturnable<Boolean> cir) {
		if (blockIn instanceof VersionBlock)
			cir.setReturnValue(false);
	}

}
