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
import dev.l3g7.griefer_utils.features.world.furniture.block.multi.DivingBoardBlock;
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
import static dev.l3g7.griefer_utils.features.world.furniture.block.multi.DivingBoardBlock.DivingBoardPart.BASE;
import static dev.l3g7.griefer_utils.features.world.furniture.block.multi.DivingBoardBlock.DivingBoardPart.BOARD;
import static dev.l3g7.griefer_utils.features.world.furniture.block.multi.DivingBoardBlock.PART;

public class DivingBoardVersionBlock extends VersionBlock {

	public DivingBoardVersionBlock(ModBlock modBlock) {
		super(modBlock);
	}

	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		EnumFacing direction = state.getValue(DIRECTION);
		DivingBoardBlock.DivingBoardPart part = state.getValue(PART);
		BlockPos otherPos = pos.offset(part == BASE ? direction : direction.getOpposite());
		IBlockState otherBlockState = worldIn.getBlockState(otherPos);

		if (otherBlockState.getBlock() == this && otherBlockState.getValue(PART) != part) {
			worldIn.setBlockState(otherPos, Blocks.air.getDefaultState(), 35);
			worldIn.playAuxSFXAtEntity(player, 2001, otherPos, getStateId(otherBlockState));
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		worldIn.setBlockState(pos.offset(placer.getHorizontalFacing()), Util.withProperties(getDefaultState(),
				PART, BOARD,
				DIRECTION, placer.getHorizontalFacing()
		), 3);
	}

}
