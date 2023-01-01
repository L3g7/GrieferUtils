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

package dev.l3g7.griefer_utils.features.world.furniture.block.general;

import dev.l3g7.griefer_utils.features.world.furniture.block.type.DefaultHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

import java.util.List;

import static dev.l3g7.griefer_utils.features.world.furniture.block.general.TrapDoorBlock.DoorHalf.BOTTOM;
import static dev.l3g7.griefer_utils.features.world.furniture.block.general.TrapDoorBlock.DoorHalf.TOP;
import static java.util.Collections.singletonList;

public class TrapDoorBlock extends DefaultHorizontalBlock {
	public static final PropertyEnum<DoorHalf> HALF = PropertyEnum.create("half", DoorHalf.class);
	protected static final AxisAlignedBB EAST_OPEN_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 0.1875, 1.0, 1.0);
	protected static final AxisAlignedBB WEST_OPEN_AABB = new AxisAlignedBB(0.8125, 0.0, 0.0, 1.0, 1.0, 1.0);
	protected static final AxisAlignedBB SOUTH_OPEN_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 0.1875);
	protected static final AxisAlignedBB NORTH_OPEN_AABB = new AxisAlignedBB(0.0, 0.0, 0.8125, 1.0, 1.0, 1.0);
	protected static final AxisAlignedBB BOTTOM_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.1875, 1.0);
	protected static final AxisAlignedBB TOP_AABB = new AxisAlignedBB(0.0, 0.8125, 0.0, 1.0, 1.0, 1.0);

	public TrapDoorBlock(BlockProperties blockProperties) {
		super(blockProperties);
	}

	protected static EnumFacing getFacing(int meta) {
		switch (meta & 3) {
			case 0:  return EnumFacing.NORTH;
			case 1:  return EnumFacing.SOUTH;
			case 2:  return EnumFacing.WEST;
			default: return EnumFacing.EAST;
		}
	}

	protected static int getMetaForFacing(EnumFacing facing) {
		switch (facing) {
			case NORTH: return 0;
			case SOUTH: return 1;
			case WEST:  return 2;
			default:    return 3;
		}
	}

	@Override
	public void addProperties(List<IProperty<?>> properties) {
		super.addProperties(properties);
		properties.add(HALF);
	}

	@Override
	public IBlockState getDefaultState(IBlockState defaultState) {
		return super.getDefaultState(defaultState).withProperty(HALF, BOTTOM);
	}

	@Override
	public List<AxisAlignedBB> getShapes(IBlockState blockState, List<AxisAlignedBB> providedShapes) {
		if (!blockState.getValue(OPEN))
			return singletonList(blockState.getValue(HALF) == TOP ? TOP_AABB : BOTTOM_AABB);

		switch (blockState.getValue(DIRECTION)) {
			case SOUTH: return singletonList(SOUTH_OPEN_AABB);
			case WEST:  return singletonList(WEST_OPEN_AABB);
			case EAST:  return singletonList(EAST_OPEN_AABB);
			default:    return singletonList(NORTH_OPEN_AABB);
		}
	}

	@Override
	public String getVersionBlockClass() {
		return "TrapDoorVersionBlock";
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return Util.withProperties(getDefaultState(),
				DIRECTION, getFacing(meta),
				OPEN, (meta & 4) != 0,
				HALF, (meta & 8) == 0 ? BOTTOM : TOP
		);
	}

	@Override
	public int getMetaFromState(IBlockState blockState) {
		int i = 0;
		i |= getMetaForFacing(blockState.getValue(DIRECTION));

		if (blockState.getValue(OPEN))
			i |= 4;
		if (blockState.getValue(HALF) == TOP)
			i |= 8;

		return i;
	}

	public enum DoorHalf implements IStringSerializable {
		TOP,
		BOTTOM;

		@Override
		public String getName() {
			return name().toLowerCase();
		}

	}

}