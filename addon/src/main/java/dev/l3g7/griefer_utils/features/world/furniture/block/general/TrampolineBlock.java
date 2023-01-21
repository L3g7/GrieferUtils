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

package dev.l3g7.griefer_utils.features.world.furniture.block.general;

import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureConnectingBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Arrays;
import java.util.List;

public class TrampolineBlock extends FurnitureConnectingBlock {

	public static final PropertyBool CORNER_NORTH_WEST = PropertyBool.create("corner_north_west");
	public static final PropertyBool CORNER_NORTH_EAST = PropertyBool.create("corner_north_east");
	public static final PropertyBool CORNER_SOUTH_EAST = PropertyBool.create("corner_south_east");
	public static final PropertyBool CORNER_SOUTH_WEST = PropertyBool.create("corner_south_west");

	public TrampolineBlock(BlockProperties properties) {
		super(properties);
	}

	@Override
	public IBlockState getDefaultState(IBlockState defaultState) {
		return Util.withSameValueProperties(super.getDefaultState(defaultState), false, CORNER_NORTH_WEST, CORNER_NORTH_EAST, CORNER_SOUTH_EAST, CORNER_SOUTH_WEST);
	}

	@Override
	public IBlockState getActualState(IBlockState state, BlockPos blockPosition, IBlockAccess world) {
		boolean north = world.getBlockState(blockPosition.north()).getBlock() == getBlockHandle();
		boolean east = world.getBlockState(blockPosition.east()).getBlock() == getBlockHandle();
		boolean south = world.getBlockState(blockPosition.south()).getBlock() == getBlockHandle();
		boolean west = world.getBlockState(blockPosition.west()).getBlock() == getBlockHandle();

		boolean cornerNorthWest = north && west && world.getBlockState(blockPosition.north().west()).getBlock() != getBlockHandle();
		boolean cornerNorthEast = north && east && world.getBlockState(blockPosition.north().east()).getBlock() != getBlockHandle();
		boolean cornerSouthEast = south && east && world.getBlockState(blockPosition.south().east()).getBlock() != getBlockHandle();
		boolean cornerSouthWest = south && west && world.getBlockState(blockPosition.south().west()).getBlock() != getBlockHandle();

		return Util.withProperties(state,
				NORTH, north,
				EAST, east,
				SOUTH, south,
				WEST, west,
				CORNER_NORTH_WEST, cornerNorthWest,
				CORNER_NORTH_EAST, cornerNorthEast,
				CORNER_SOUTH_EAST, cornerSouthEast,
				CORNER_SOUTH_WEST, cornerSouthWest
		);
	}

	@Override
	public void addProperties(List<IProperty<?>> properties) {
		super.addProperties(properties);
		properties.addAll(Arrays.asList(CORNER_NORTH_WEST, CORNER_NORTH_EAST, CORNER_SOUTH_EAST, CORNER_SOUTH_WEST));
	}

}
