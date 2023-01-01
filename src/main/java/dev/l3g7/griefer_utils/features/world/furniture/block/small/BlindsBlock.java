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

package dev.l3g7.griefer_utils.features.world.furniture.block.small;

import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.List;

public class BlindsBlock extends FurnitureHorizontalBlock {
	public static final PropertyBool OPEN = PropertyBool.create("open");
	public static final PropertyBool EXTENSION = PropertyBool.create("extension");

	public BlindsBlock(BlockProperties properties) {
		super(properties);
	}

	@Override
	public IBlockState getDefaultState(IBlockState defaultState) {
		return super.getDefaultState(defaultState).withProperty(OPEN, true).withProperty(EXTENSION, false);
	}

	@Override
	public IBlockState getActualState(IBlockState state, BlockPos blockPosition, IBlockAccess world) {
		IBlockState aboveState = world.getBlockState(blockPosition.up());
		boolean isExtension = Util.getCustomBlock(aboveState) == this && aboveState.getValue(DIRECTION) == state.getValue(DIRECTION);
		return state.withProperty(EXTENSION, isExtension);
	}

	@Override
	public boolean isTransparent(IBlockState blockState) {
		return !blockState.getValue(OPEN);
	}

	@Override
	public void addProperties(List<IProperty<?>> properties) {
		super.addProperties(properties);
		properties.add(OPEN);
		properties.add(EXTENSION);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		boolean open = meta > 3;
		if (meta > 3)
			meta -= 4;

		return this.getDefaultState().withProperty(DIRECTION, Util.getHorizontal(meta & 3)).withProperty(OPEN, open);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int direction = state.getValue(DIRECTION).ordinal() - 2;
		if (state.getValue(OPEN))
			direction += 4;

		return direction;
	}

}