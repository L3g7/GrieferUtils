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

package dev.l3g7.griefer_utils.features.world.furniture.block;

import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;

import java.util.Arrays;
import java.util.List;

public class FurnitureConnectingBlock extends FurnitureBlock {

	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool EAST = PropertyBool.create("east");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool WEST = PropertyBool.create("west");

	public FurnitureConnectingBlock(BlockProperties properties) {
		super(properties);
	}

	@Override
	public IBlockState getDefaultState(IBlockState defaultState) {
		return Util.withSameValueProperties(super.getDefaultState(defaultState), false, NORTH, EAST, SOUTH, WEST);
	}

	@Override
	public void addProperties(List<IProperty<?>> properties) {
		super.addProperties(properties);
		properties.addAll(Arrays.asList(NORTH, EAST, SOUTH, WEST));
	}
}
