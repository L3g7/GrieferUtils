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

package dev.l3g7.griefer_utils.features.world.furniture.block.multi;

import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;

import java.util.List;

import static dev.l3g7.griefer_utils.features.world.furniture.block.multi.DivingBoardBlock.DivingBoardPart.BASE;
import static dev.l3g7.griefer_utils.features.world.furniture.block.multi.DivingBoardBlock.DivingBoardPart.BOARD;

public class DivingBoardBlock extends FurnitureHorizontalBlock {

	public static final PropertyEnum<DivingBoardPart> PART = PropertyEnum.create("part", DivingBoardPart.class);

	public DivingBoardBlock(BlockProperties blockProperties) {
		super(blockProperties);
	}

	@Override
	public IBlockState getDefaultState(IBlockState defaultState) {
		return super.getDefaultState(defaultState).withProperty(PART, BASE);
	}

	@Override
	public void addProperties(List<IProperty<?>> properties) {
		super.addProperties(properties);
		properties.add(PART);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		DivingBoardPart divingBoardPart = (meta > 3) ? BOARD : BASE;
		if (meta > 3)
			meta -= 4;

		return Util.withProperties(getDefaultState(),
				DIRECTION, Util.getHorizontal(meta & 3),
				PART, divingBoardPart
		);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int direction = state.getValue(DIRECTION).ordinal() - 2;
		if (state.getValue(PART) == BOARD)
			direction += 4;

		return direction;
	}

	@Override
	public boolean isValidPosition(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPosition) {
		return blockAccess.getBlockState(blockPosition.offset(blockState.getValue(DIRECTION))).getBlock() == Blocks.air;
	}

	public enum DivingBoardPart implements IStringSerializable {
		BASE,
		BOARD;

		@Override
		public String toString() {
			return this.getName();
		}

		@Override
		public String getName() {
			return (this == BASE) ? "base" : "board";
		}

	}

}
