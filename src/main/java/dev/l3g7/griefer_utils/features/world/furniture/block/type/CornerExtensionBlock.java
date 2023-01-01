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

package dev.l3g7.griefer_utils.features.world.furniture.block.type;

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

import static dev.l3g7.griefer_utils.features.world.furniture.block.type.CornerExtensionBlock.Type.*;

public class CornerExtensionBlock extends FurnitureHorizontalBlock {
	public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);

	public CornerExtensionBlock(BlockProperties properties) {
		super(properties);
	}

	@Override
	public IBlockState getDefaultState(IBlockState defaultState) {
		return super.getDefaultState(defaultState).withProperty(TYPE, SINGLE);
	}

	@Override
	public IBlockState getActualState(IBlockState state, BlockPos pos, IBlockAccess world) {
		EnumFacing dir = state.getValue(DIRECTION);
		boolean left = isSofa(world, pos, dir.rotateYCCW(), dir) || isSofa(world, pos, dir.rotateYCCW(), dir.rotateYCCW());
		boolean right = isSofa(world, pos, dir.rotateY(), dir) || isSofa(world, pos, dir.rotateY(), dir.rotateY());
		boolean cornerLeft = isSofa(world, pos, dir.getOpposite(), dir.rotateYCCW());
		boolean cornerRight = isSofa(world, pos, dir.getOpposite(), dir.rotateY());

		if (cornerLeft) return state.withProperty(TYPE, CORNER_LEFT);
		if (cornerRight) return state.withProperty(TYPE, CORNER_RIGHT);
		if (left && right) return state.withProperty(TYPE, MIDDLE);
		if (left) return state.withProperty(TYPE, RIGHT);
		if (right) return state.withProperty(TYPE, LEFT);

		return state.withProperty(TYPE, SINGLE);
	}

	private boolean isSofa(IBlockAccess world, BlockPos source, EnumFacing direction, EnumFacing targetDirection) {
		IBlockState state = world.getBlockState(source.offset(direction));
		if (Util.getCustomBlock(state) == this) {
			EnumFacing sofaDirection = state.getValue(DIRECTION);
			return sofaDirection.equals(targetDirection);
		}
		return false;
	}

	@Override
	public void addProperties(List<IProperty<?>> properties) {
		super.addProperties(properties);
		properties.add(TYPE);
	}

	public enum Type implements IStringSerializable {
		SINGLE,
		LEFT,
		RIGHT,
		MIDDLE,
		CORNER_LEFT,
		CORNER_RIGHT;

		@Override
		public String getName() {
			return name().toLowerCase();
		}

	}

}