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

package dev.l3g7.griefer_utils.features.world.furniture.block.version_specific;

import dev.l3g7.griefer_utils.features.world.furniture.block.ModBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import static dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock.DIRECTION;
import static dev.l3g7.griefer_utils.features.world.furniture.block.small.BlindsBlock.OPEN;
import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.UP;

public class BlindsVersionBlock extends VersionBlock {

	public BlindsVersionBlock(ModBlock modBlock) {
		super(modBlock);
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing facing, float hitX, float hitY, float hitZ) {
		toggleBlinds(world, pos, !state.getValue(OPEN), state.getValue(DIRECTION), 7);

		if (!world.isRemote)
			world.playSoundEffect(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, "griefer_utils:block.blinds." + (state.getValue(OPEN) ? "close" : "open"), 0.5f, world.rand.nextFloat() * 0.1f + 0.8f);

		return true;
	}

	private void toggleBlinds(World world, BlockPos pos, boolean targetOpen, EnumFacing targetDirection, int depth) {
		if (depth <= 0)
			return;

		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this)
			return;

		boolean open = state.getValue(OPEN);
		EnumFacing direction = state.getValue(DIRECTION);

		if (open == targetOpen || !direction.equals(targetDirection))
			return;

		world.setBlockState(pos, state.withProperty(OPEN, targetOpen), 3);
		toggleBlinds(world, pos.offset(targetDirection.rotateY()), targetOpen, targetDirection, depth - 1);
		toggleBlinds(world, pos.offset(targetDirection.rotateYCCW()), targetOpen, targetDirection, depth - 1);
		toggleBlinds(world, pos.offset(UP), targetOpen, targetDirection, depth - 1);
		toggleBlinds(world, pos.offset(DOWN), targetOpen, targetDirection, depth - 1);
	}

}
