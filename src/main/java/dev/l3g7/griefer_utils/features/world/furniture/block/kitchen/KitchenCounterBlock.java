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

package dev.l3g7.griefer_utils.features.world.furniture.block.kitchen;

import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;

import java.util.List;

import static dev.l3g7.griefer_utils.features.world.furniture.block.kitchen.KitchenCounterBlock.Type.*;

public class KitchenCounterBlock extends FurnitureHorizontalBlock {
	public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);

	public KitchenCounterBlock(BlockProperties properties) {
		super(properties);
	}

	@Override
	public IBlockState getDefaultState(IBlockState defaultState) {
		return super.getDefaultState(defaultState).withProperty(TYPE, DEFAULT);
	}

	@Override
	public IBlockState getActualState(IBlockState state, BlockPos blockPosition, IBlockAccess world) {
		EnumFacing direction = state.getValue(DIRECTION);
		IBlockState frontState = world.getBlockState(blockPosition.offset(direction.getOpposite()));
		if (Util.getCustomBlock(frontState) instanceof KitchenCounterBlock) {
			if (frontState.getValue(DIRECTION) == direction.rotateY())
				return state.withProperty(TYPE, RIGHT_CORNER);
			if (frontState.getValue(DIRECTION) == direction.rotateYCCW())
				return state.withProperty(TYPE, LEFT_CORNER);
		}

		IBlockState backState = world.getBlockState(blockPosition.offset(direction));
		if (Util.getCustomBlock(backState) instanceof KitchenCounterBlock) {
			if (backState.getValue(DIRECTION) == direction.rotateY()) {
				IBlockState leftState = world.getBlockState(blockPosition.offset(direction.rotateYCCW()));
				if (!(Util.getCustomBlock(leftState) instanceof KitchenCounterBlock) || leftState.getValue(DIRECTION) == direction.getOpposite())
					return state.withProperty(TYPE, LEFT_CORNER_INVERTED);
			}

			if (backState.getValue(DIRECTION) == direction.rotateYCCW()) {
				IBlockState rightState = world.getBlockState(blockPosition.offset(direction.rotateY()));
				if (!(Util.getCustomBlock(rightState) instanceof KitchenCounterBlock) || rightState.getValue(DIRECTION) == direction.getOpposite())
					return state.withProperty(TYPE, RIGHT_CORNER_INVERTED);
			}
		}

		return state.withProperty(TYPE, DEFAULT);
	}

	@Override
	public void addProperties(List<IProperty<?>> properties) {
		super.addProperties(properties);
		properties.add(TYPE);
	}

	public enum Type implements IStringSerializable {
		DEFAULT,
		LEFT_CORNER,
		RIGHT_CORNER,
		LEFT_CORNER_INVERTED,
		RIGHT_CORNER_INVERTED;

		@Override
		public String getName() {
			return name().toLowerCase();
		}

	}

}
