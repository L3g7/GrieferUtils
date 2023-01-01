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
import dev.l3g7.griefer_utils.features.world.furniture.block.type.DoubleBlock;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import static dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock.DIRECTION;
import static dev.l3g7.griefer_utils.features.world.furniture.block.type.DoubleBlock.DoublePart.LEFT;
import static dev.l3g7.griefer_utils.features.world.furniture.block.type.DoubleBlock.DoublePart.RIGHT;
import static dev.l3g7.griefer_utils.features.world.furniture.block.type.DoubleBlock.PART;

public class DoubleVersionBlock extends VersionBlock {

	private final DoubleBlock doubleBlock;

	public DoubleVersionBlock(ModBlock modBlock) {
		super(modBlock);
		doubleBlock = (DoubleBlock) modBlock;
	}

	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		EnumFacing direction = state.getValue(DIRECTION);
		DoubleBlock.DoublePart part = state.getValue(PART);
		EnumFacing leftDirection = direction.rotateYCCW();
		BlockPos otherPosition = pos.offset(part == LEFT ? leftDirection.getOpposite() : leftDirection);
		IBlockState otherBlockState = worldIn.getBlockState(otherPosition);

		if (otherBlockState.getBlock() == this && otherBlockState.getValue(PART) != part) {
			worldIn.setBlockState(otherPosition, Blocks.air.getDefaultState(), 3);
			worldIn.playAuxSFXAtEntity(player, doubleBlock.getDropSfx(), otherPosition, getStateId(otherBlockState));
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		EnumFacing targetDirection = placer.getHorizontalFacing();
		EnumFacing leftDirection = targetDirection.rotateYCCW();

		worldIn.setBlockState(pos.offset(leftDirection.getOpposite()), Util.withProperties(getDefaultState(),
				PART, RIGHT,
				DIRECTION, targetDirection
		), 3);
	}

}
