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

import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureConnectingBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.ModBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.fence.UpgradedFenceBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.fence.UpgradedGateBlock;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public class UpgradedFenceVersionBlock extends VersionBlock {

	public UpgradedFenceVersionBlock(ModBlock modBlock) {
		super(modBlock);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		boolean north = canConnectToBlock(world, pos, EnumFacing.NORTH);
		boolean east = canConnectToBlock(world, pos, EnumFacing.EAST);
		boolean south = canConnectToBlock(world, pos, EnumFacing.SOUTH);
		boolean west = canConnectToBlock(world, pos, EnumFacing.WEST);
		boolean post = (!north || east || !south || west) && (north || !east || south || !west);

		return Util.withProperties(state, FurnitureConnectingBlock.NORTH, north, FurnitureConnectingBlock.EAST, east, FurnitureConnectingBlock.SOUTH, south, FurnitureConnectingBlock.WEST, west, UpgradedFenceBlock.POST, post);
	}

	private boolean canConnectToBlock(IBlockAccess world, BlockPos pos, EnumFacing direction) {
		BlockPos offsetPos = pos.offset(direction);
		IBlockState offsetState = world.getBlockState(offsetPos);
		if (Util.getCustomBlock(offsetState) == getModBlock())
			return true;

		if (!(Util.getCustomBlock(offsetState) instanceof UpgradedGateBlock))
			return offsetState.getBlock().isFullBlock();

		EnumFacing gateDirection = offsetState.getValue(FurnitureHorizontalBlock.DIRECTION);
		UpgradedGateBlock.DoorHingeSide hingeSide = offsetState.getValue(UpgradedGateBlock.HINGE);
		EnumFacing hingeFace = (hingeSide == UpgradedGateBlock.DoorHingeSide.LEFT) ? gateDirection.rotateYCCW() : gateDirection.rotateY();

		return direction == hingeFace.getOpposite() || (!offsetState.getValue(UpgradedGateBlock.DOUBLE) && direction.getAxis() != gateDirection.getAxis());
	}

	@Override
	public boolean isFence() {
		return true;
	}

	public int getLightOpacity() {
		return 1;
	}

}
