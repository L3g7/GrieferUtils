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

package dev.l3g7.griefer_utils.features.world.furniture.block.version_specific;

import dev.l3g7.griefer_utils.features.world.furniture.block.ModBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PlantyVersionBlock extends VersionBlock {

	public PlantyVersionBlock(ModBlock modBlock) {
		super(modBlock);
	}

	public boolean canSustainBush(IBlockState state) {
		return (state.getBlock() instanceof VersionBlock && ((VersionBlock)state.getBlock()).getModBlock().isCanSustainBush())
				|| state.getBlock() == Blocks.grass
				|| state.getBlock() == Blocks.dirt
				|| state.getBlock() == Blocks.farmland;
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return super.canPlaceBlockAt(worldIn, pos) && canSustainBush(worldIn.getBlockState(pos.down()));
	}

}
