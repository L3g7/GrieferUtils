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

import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;

import java.util.List;

import static dev.l3g7.griefer_utils.features.world.furniture.block.type.DoubleBlock.DoublePart.LEFT;

public class DoubleBlock extends FurnitureHorizontalBlock {

	public static final PropertyEnum<DoublePart> PART = PropertyEnum.create("part", DoublePart.class);

	public DoubleBlock(BlockProperties blockProperties) {
		super(blockProperties);
	}

	@Override
	public IBlockState getDefaultState(IBlockState defaultState) {
		return super.getDefaultState(defaultState).withProperty(PART, LEFT);
	}

	@Override
	public void addProperties(List<IProperty<?>> properties) {
		super.addProperties(properties);
		properties.add(PART);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta > 7)
			meta = 7;

		return Util.withProperties(getDefaultState(),
				DIRECTION, Util.getHorizontal(meta % 4),
				PART, DoublePart.values()[meta / 4]
		);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(PART).ordinal() * 4 + state.getValue(DIRECTION).ordinal() - 2;
	}

	@Override
	public String getVersionBlockClass() {
		return "DoubleVersionBlock";
	}

	@Override
	public boolean isValidPosition(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPosition) {
		EnumFacing direction = blockState.getValue(DIRECTION);
		return blockAccess.getBlockState(blockPosition.offset(direction.rotateYCCW().getOpposite())).getBlock() == Blocks.air;
	}

	public int getDropSfx() {
		return 2001;
	}

	public enum DoublePart implements IStringSerializable {
		LEFT,
		RIGHT;

		@Override
		public String getName() {
			return name().toLowerCase();
		}

	}

}
