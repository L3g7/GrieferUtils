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

package dev.l3g7.griefer_utils.injection.mixin.furniture.world;

import dev.l3g7.griefer_utils.features.world.furniture.block.version_specific.VersionBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {

	@Inject(method = { "doesBlockHaveSolidTopSurface" }, at = { @At("HEAD") }, cancellable = true)
	private static void injectDoesBlockHaveSolidTopSurface(IBlockAccess blockAccess, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (blockAccess.getBlockState(pos).getBlock() instanceof VersionBlock)
			cir.setReturnValue(true);
	}

}
