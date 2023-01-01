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

package dev.l3g7.griefer_utils.injection.mixin.furniture.entity;

import dev.l3g7.griefer_utils.features.world.furniture.block.version_specific.VersionBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {

	public MixinEntityLivingBase(World worldIn) {
		super(worldIn);
	}

	@Inject(method = "isOnLadder", at = @At("HEAD"), cancellable = true)
	public void injectIsOnLadder(CallbackInfoReturnable<Boolean> cir) {
		int x = MathHelper.floor_double(posX);
		int y = MathHelper.floor_double(getEntityBoundingBox().minY);
		int z = MathHelper.floor_double(posZ);
		Block block = worldObj.getBlockState(new BlockPos(x, y, z)).getBlock();

		cir.setReturnValue((block == Blocks.ladder
			|| block == Blocks.vine
			|| (block instanceof VersionBlock
			&& ((VersionBlock)block).getModBlock().isClimbable())
			) && (!(((Object) this) instanceof EntityPlayer)
			|| !((EntityPlayer)(Object) this).isSpectator()));
	}

}
