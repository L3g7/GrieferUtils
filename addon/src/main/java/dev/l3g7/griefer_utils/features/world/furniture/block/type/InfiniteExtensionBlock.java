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

package dev.l3g7.griefer_utils.features.world.furniture.block.type;

import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureConnectingBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class InfiniteExtensionBlock extends FurnitureConnectingBlock {

	public InfiniteExtensionBlock(BlockProperties properties) {
		super(properties);
	}

	@Override
	public IBlockState getActualState(IBlockState state, BlockPos blockPosition, IBlockAccess world) {
		boolean north = world.getBlockState(blockPosition.north()).getBlock() == getBlockHandle();
		boolean east = world.getBlockState(blockPosition.east()).getBlock() == getBlockHandle();
		boolean south = world.getBlockState(blockPosition.south()).getBlock() == getBlockHandle();
		boolean west = world.getBlockState(blockPosition.west()).getBlock() == getBlockHandle();

		return Util.withProperties(state, NORTH, north, EAST, east, SOUTH, south, WEST, west);
	}

}
