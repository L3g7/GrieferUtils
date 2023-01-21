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

import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.ModBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.fence.UpgradedGateBlock;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class UpgradedGateVersionBlock extends VersionBlock {

	public UpgradedGateVersionBlock(ModBlock modBlock) {
		super(modBlock);
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing facing, float hitX, float hitY, float hitZ) {
		EnumFacing direction = state.getValue(FurnitureHorizontalBlock.DIRECTION);
		boolean open = state.getValue(FurnitureBlock.OPEN);
		if (facing.getAxis() != direction.getAxis() && (!open || facing.getAxis().isVertical()))
			return false;

		UpgradedGateBlock.DoorHingeSide hingeSide = state.getValue(UpgradedGateBlock.HINGE);
		openGate(state, world, pos, direction, facing, !open);
		openDoubleGate(world, pos, direction, facing, hingeSide, !open);
		openAdjacentGate(world, pos, direction, EnumFacing.UP, facing, hingeSide, !open, 5);
		openAdjacentGate(world, pos, direction, EnumFacing.DOWN, facing, hingeSide, !open, 5);
		world.playAuxSFXAtEntity(playerIn, open ? 1006 : 1003, pos, 0);
		return true;
	}

	private void openGate(IBlockState state, World world, BlockPos pos, EnumFacing direction, EnumFacing hitFace, boolean open) {
		state = state.withProperty(FurnitureBlock.OPEN, open);

		if (open && hitFace == direction) {
			state = Util.withProperties(state,
					UpgradedGateBlock.HINGE, getOppositeHinge(state.getValue(UpgradedGateBlock.HINGE)),
					FurnitureHorizontalBlock.DIRECTION, hitFace.getOpposite()
			);
		}

		world.setBlockState(pos, state, 3);
	}

	private void openAdjacentGate(World world, BlockPos pos, EnumFacing direction, EnumFacing offset, EnumFacing hitFace, UpgradedGateBlock.DoorHingeSide hingeSide, boolean open, int limit) {
		if (limit <= 0)
			return;

		BlockPos offsetPos = pos.offset(offset);
		IBlockState state = world.getBlockState(offsetPos);


		if (state.getBlock() != this || state.getValue(FurnitureHorizontalBlock.DIRECTION) != direction || state.getValue(UpgradedGateBlock.HINGE) != hingeSide || state.getValue(FurnitureBlock.OPEN) == open)
			return;

		openGate(state, world, offsetPos, direction, hitFace, open);
		openDoubleGate(world, offsetPos, direction, hitFace, hingeSide, open);
		openAdjacentGate(world, offsetPos, direction, offset, hitFace, hingeSide, open, limit - 1);
	}

	private void openDoubleGate(World world, BlockPos pos, EnumFacing direction, EnumFacing hitFace, UpgradedGateBlock.DoorHingeSide hingeSide, boolean open) {
		BlockPos adjacentPos = pos.offset((hingeSide == UpgradedGateBlock.DoorHingeSide.LEFT) ? direction.rotateY() : direction.rotateYCCW());
		IBlockState adjacentState = world.getBlockState(adjacentPos);

		if (Util.getCustomBlock(adjacentState) != getModBlock() || adjacentState.getValue(FurnitureHorizontalBlock.DIRECTION).getAxis() != direction.getAxis())
			return;

		if (adjacentState.getValue(UpgradedGateBlock.HINGE) == hingeSide) {
			adjacentState = Util.withProperties(adjacentState,
					FurnitureHorizontalBlock.DIRECTION, adjacentState.getValue(FurnitureHorizontalBlock.DIRECTION).getOpposite(),
					UpgradedGateBlock.HINGE, getOppositeHinge(adjacentState.getValue(UpgradedGateBlock.HINGE))
			);
		}

		openGate(adjacentState, world, adjacentPos, direction, hitFace, open);
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(UpgradedGateBlock.HINGE, getHingeSide(hitX, hitZ, placer));
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		EnumFacing facing = state.getValue(FurnitureHorizontalBlock.DIRECTION);
		EnumFacing offset = (state.getValue(UpgradedGateBlock.HINGE) == UpgradedGateBlock.DoorHingeSide.LEFT) ? facing.rotateY() : facing.rotateYCCW();
		IBlockState adjacentBlock = worldIn.getBlockState(pos.offset(offset));
		return state.withProperty(UpgradedGateBlock.DOUBLE, adjacentBlock.getBlock() == this);
	}

	private UpgradedGateBlock.DoorHingeSide getHingeSide(float hitX, float hitZ, EntityLivingBase placer) {
		int offsetX = placer.getHorizontalFacing().getDirectionVec().getX();
		int offsetZ = placer.getHorizontalFacing().getDirectionVec().getZ();
		boolean side = (offsetX < 0 && hitZ < 0.5) || (offsetX > 0 && hitZ > 0.5) || (offsetZ < 0 && hitX > 0.5) || (offsetZ > 0 && hitX < 0.5);

		return side ? UpgradedGateBlock.DoorHingeSide.RIGHT : UpgradedGateBlock.DoorHingeSide.LEFT;
	}

	private UpgradedGateBlock.DoorHingeSide getOppositeHinge(UpgradedGateBlock.DoorHingeSide hingeSide) {
		return hingeSide == UpgradedGateBlock.DoorHingeSide.LEFT ? UpgradedGateBlock.DoorHingeSide.RIGHT : UpgradedGateBlock.DoorHingeSide.LEFT;
	}

	public int getLightOpacity() {
		return 1;
	}

}