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
import dev.l3g7.griefer_utils.features.world.furniture.block.general.TrapDoorBlock;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import static dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureBlock.OPEN;
import static dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock.DIRECTION;
import static net.minecraft.util.EnumFacing.UP;

public class TrapDoorVersionBlock extends VersionBlock {

	public TrapDoorVersionBlock(ModBlock modBlock) {
		super(modBlock);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return !worldIn.getBlockState(pos).getValue(OPEN);
	}

	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		if (worldIn.isRemote)
			return;

		boolean powered = worldIn.isBlockPowered(pos);
		if ((!powered && !neighborBlock.canProvidePower()) || state.getValue(OPEN) != powered)
			return;

		worldIn.setBlockState(pos, state.withProperty(OPEN, powered), 2);
		playSound(null, worldIn, pos, powered);
	}

	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing facing, float hitX, float hitY, float hitZ) {
		boolean open = !state.getValue(OPEN);
		state = state.withProperty(OPEN, open);

		worldIn.setBlockState(pos, state, 2);
		playSound(playerIn, worldIn, pos, open);
		return true;
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		IBlockState iblockstate = getDefaultState();

		if (facing.getAxis().isHorizontal()) {
			iblockstate = Util.withProperties(iblockstate,
					DIRECTION, facing,
					OPEN, false,
					TrapDoorBlock.HALF, hitY > 0.5f ? TrapDoorBlock.DoorHalf.TOP : TrapDoorBlock.DoorHalf.BOTTOM
			);
		} else {
			iblockstate = Util.withProperties(iblockstate,
					DIRECTION, placer.getHorizontalFacing().getOpposite(),
					OPEN, false,
					TrapDoorBlock.HALF, facing == UP ? TrapDoorBlock.DoorHalf.BOTTOM : TrapDoorBlock.DoorHalf.TOP
			);
		}

		return worldIn.isBlockPowered(pos) ? iblockstate.withProperty(OPEN, true) : iblockstate;
	}

	protected void playSound(EntityPlayer player, World worldIn, BlockPos pos, boolean open) {
		worldIn.playAuxSFXAtEntity(player, open ? 1003 : 1006, pos, 0);
	}
}