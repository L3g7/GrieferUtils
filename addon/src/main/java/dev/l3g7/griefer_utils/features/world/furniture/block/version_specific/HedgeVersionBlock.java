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
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;

import static dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureConnectingBlock.*;

public class HedgeVersionBlock extends VersionBlock {

	public HedgeVersionBlock(ModBlock modBlock) {
		super(modBlock);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		boolean north = canConnectToBlock(world, pos, EnumFacing.NORTH);
		boolean east = canConnectToBlock(world, pos, EnumFacing.EAST);
		boolean south = canConnectToBlock(world, pos, EnumFacing.SOUTH);
		boolean west = canConnectToBlock(world, pos, EnumFacing.WEST);

		return Util.withProperties(state,
				NORTH, north,
				EAST, east,
				SOUTH, south,
				WEST, west
		);
	}

	private boolean canConnectToBlock(IBlockAccess world, BlockPos pos, EnumFacing direction) {
		BlockPos offsetPos = pos.offset(direction);
		IBlockState offsetState = world.getBlockState(offsetPos);

		return offsetState.getBlock() == this || offsetState.getBlock().isFullBlock();
	}

	public int getLightOpacity() {
		return 1;
	}

	@Override
	public EnumWorldBlockLayer getBlockLayer() {
		return Minecraft.getMinecraft().gameSettings.fancyGraphics ? EnumWorldBlockLayer.CUTOUT : EnumWorldBlockLayer.SOLID;
	}

	@Override
	public boolean isFence() {
		return true;
	}

	public int getBlockColor() {
		return ColorizerFoliage.getFoliageColor(0.5, 1.0);
	}

	public int getRenderColor(IBlockState state) {
		return ColorizerFoliage.getFoliageColorBasic();
	}

	public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
		return BiomeColorHelper.getFoliageColorAtPos(worldIn, pos);
	}

}
