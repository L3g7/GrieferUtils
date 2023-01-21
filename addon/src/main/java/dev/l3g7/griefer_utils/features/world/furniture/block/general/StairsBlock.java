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

import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.type.DefaultHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.features.world.furniture.block.general.StairsBlock.EnumHalf.BOTTOM;
import static dev.l3g7.griefer_utils.features.world.furniture.block.general.StairsBlock.EnumHalf.TOP;
import static dev.l3g7.griefer_utils.features.world.furniture.block.general.StairsBlock.EnumShape.*;

public class StairsBlock extends DefaultHorizontalBlock {

	public static final PropertyEnum<EnumHalf> HALF = PropertyEnum.create("half", EnumHalf.class);
	public static final PropertyEnum<EnumShape>  SHAPE = PropertyEnum.create("shape", EnumShape.class);
	protected static final AxisAlignedBB AABB_SLAB_TOP = new AxisAlignedBB(0.0, 0.5, 0.0, 1.0, 1.0, 1.0);
	protected static final AxisAlignedBB AABB_QTR_TOP_WEST = new AxisAlignedBB(0.0, 0.5, 0.0, 0.5, 1.0, 1.0);
	protected static final AxisAlignedBB AABB_QTR_TOP_EAST = new AxisAlignedBB(0.5, 0.5, 0.0, 1.0, 1.0, 1.0);
	protected static final AxisAlignedBB AABB_QTR_TOP_NORTH = new AxisAlignedBB(0.0, 0.5, 0.0, 1.0, 1.0, 0.5);
	protected static final AxisAlignedBB AABB_QTR_TOP_SOUTH = new AxisAlignedBB(0.0, 0.5, 0.5, 1.0, 1.0, 1.0);
	protected static final AxisAlignedBB AABB_OCT_TOP_NW = new AxisAlignedBB(0.0, 0.5, 0.0, 0.5, 1.0, 0.5);
	protected static final AxisAlignedBB AABB_OCT_TOP_NE = new AxisAlignedBB(0.5, 0.5, 0.0, 1.0, 1.0, 0.5);
	protected static final AxisAlignedBB AABB_OCT_TOP_SW = new AxisAlignedBB(0.0, 0.5, 0.5, 0.5, 1.0, 1.0);
	protected static final AxisAlignedBB AABB_OCT_TOP_SE = new AxisAlignedBB(0.5, 0.5, 0.5, 1.0, 1.0, 1.0);
	protected static final AxisAlignedBB AABB_SLAB_BOTTOM = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);
	protected static final AxisAlignedBB AABB_QTR_BOT_WEST = new AxisAlignedBB(0.0, 0.0, 0.0, 0.5, 0.5, 1.0);
	protected static final AxisAlignedBB AABB_QTR_BOT_EAST = new AxisAlignedBB(0.5, 0.0, 0.0, 1.0, 0.5, 1.0);
	protected static final AxisAlignedBB AABB_QTR_BOT_NORTH = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 0.5);
	protected static final AxisAlignedBB AABB_QTR_BOT_SOUTH = new AxisAlignedBB(0.0, 0.0, 0.5, 1.0, 0.5, 1.0);
	protected static final AxisAlignedBB AABB_OCT_BOT_NW = new AxisAlignedBB(0.0, 0.0, 0.0, 0.5, 0.5, 0.5);
	protected static final AxisAlignedBB AABB_OCT_BOT_NE = new AxisAlignedBB(0.5, 0.0, 0.0, 1.0, 0.5, 0.5);
	protected static final AxisAlignedBB AABB_OCT_BOT_SW = new AxisAlignedBB(0.0, 0.0, 0.5, 0.5, 0.5, 1.0);
	protected static final AxisAlignedBB AABB_OCT_BOT_SE = new AxisAlignedBB(0.5, 0.0, 0.5, 1.0, 0.5, 1.0);

	public StairsBlock(BlockProperties blockProperties) {
		super(blockProperties);
	}

	@Override
	public void addProperties(List<IProperty<?>> properties) {
		super.addProperties(properties);
		properties.add(HALF);
		properties.add(SHAPE);
	}

	@Override
	public IBlockState getActualState(IBlockState state, BlockPos blockPosition, IBlockAccess world) {
		return super.getActualState(state, blockPosition, world).withProperty(SHAPE, getStairsShape(state, world, blockPosition));
	}

	@Override
	public List<AxisAlignedBB> getShapes(IBlockState bstate, List<AxisAlignedBB> providedShapes) {
		List<AxisAlignedBB> list = new ArrayList<>();
		list.add(bstate.getValue(HALF) == TOP ? AABB_SLAB_TOP : AABB_SLAB_BOTTOM);
		EnumShape shape = bstate.getValue(SHAPE);

		if (shape == STRAIGHT || shape == INNER_LEFT || shape == INNER_RIGHT)
			list.add(getCollQuarterBlock(bstate));
		if (shape != STRAIGHT)
			list.add(getCollEighthBlock(bstate));

		return list;
	}

	@Override
	public List<AxisAlignedBB> getCollisionShapes(IBlockState blockState, List<AxisAlignedBB> providedShapes) {
		return this.getShapes(blockState, providedShapes);
	}

	private static AxisAlignedBB getCollQuarterBlock(IBlockState bstate) {
		boolean flag = bstate.getValue(HALF) == TOP;

		switch (bstate.getValue(FurnitureHorizontalBlock.DIRECTION)) {
			case SOUTH: return flag ? AABB_QTR_BOT_SOUTH : AABB_QTR_TOP_SOUTH;
			case WEST:  return flag ? AABB_QTR_BOT_WEST : AABB_QTR_TOP_WEST;
			case EAST:  return flag ? AABB_QTR_BOT_EAST : AABB_QTR_TOP_EAST;
			default:    return flag ? AABB_QTR_BOT_NORTH : AABB_QTR_TOP_NORTH;
		}
	}

	private static AxisAlignedBB getCollEighthBlock(IBlockState bstate) {
		EnumFacing enumfacing = bstate.getValue(FurnitureHorizontalBlock.DIRECTION);
		EnumFacing enumfacing2;
		switch (bstate.getValue(SHAPE)) {
			default: {
				enumfacing2 = enumfacing;
				break;
			}
			case OUTER_RIGHT: {
				enumfacing2 = enumfacing.rotateY();
				break;
			}
			case INNER_RIGHT: {
				enumfacing2 = enumfacing.getOpposite();
				break;
			}
			case INNER_LEFT: {
				enumfacing2 = enumfacing.rotateYCCW();
				break;
			}
		}

		boolean flag = bstate.getValue(HALF) == TOP;
		switch (enumfacing2) {
			case SOUTH: return flag ? AABB_OCT_BOT_SE : AABB_OCT_TOP_SE;
			case WEST:  return flag ? AABB_OCT_BOT_SW : AABB_OCT_TOP_SW;
			case EAST:  return flag ? AABB_OCT_BOT_NE : AABB_OCT_TOP_NE;
			default:    return flag ? AABB_OCT_BOT_NW : AABB_OCT_TOP_NW;
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState iblockstate = getDefaultState().withProperty(HALF, ((meta & 4) > 0) ? TOP : BOTTOM);
		iblockstate = iblockstate.withProperty(FurnitureHorizontalBlock.DIRECTION, EnumFacing.getFront(5 - (meta & 3)));
		return iblockstate;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(HALF) == TOP ? 4 : 0;
		i |= 5 - (state.getValue(FurnitureHorizontalBlock.DIRECTION).ordinal() - 2);
		return i;
	}

	private static EnumShape getStairsShape(IBlockState state, IBlockAccess world, BlockPos pos) {
		EnumFacing direction = state.getValue(FurnitureHorizontalBlock.DIRECTION);
		IBlockState frontState = world.getBlockState(pos.offset(direction));

		if (isBlockStairs(frontState) && state.getValue(HALF) == frontState.getValue(HALF)) {
			EnumFacing frontDirection = frontState.getValue(FurnitureHorizontalBlock.DIRECTION);
			if (frontDirection.getAxis() != state.getValue(FurnitureHorizontalBlock.DIRECTION).getAxis() && isDifferentStairs(state, world, pos, frontDirection.getOpposite()))
				return (frontDirection == direction.rotateYCCW()) ? OUTER_LEFT : OUTER_RIGHT;
		}

		IBlockState backState = world.getBlockState(pos.offset(direction.getOpposite()));
		if (isBlockStairs(backState) && state.getValue(HALF) == backState.getValue(HALF)) {
			EnumFacing backDirection = backState.getValue(FurnitureHorizontalBlock.DIRECTION);
			if (backDirection.getAxis() != state.getValue(FurnitureHorizontalBlock.DIRECTION).getAxis() && isDifferentStairs(state, world, pos, backDirection))
				return backDirection == direction.rotateYCCW() ? INNER_LEFT : INNER_RIGHT;
		}

		return STRAIGHT;
	}

	private static boolean isDifferentStairs(IBlockState state, IBlockAccess world, BlockPos blockPos, EnumFacing enumFacing) {
		IBlockState iblockstate = world.getBlockState(blockPos.offset(enumFacing));
		return !isBlockStairs(iblockstate)
				|| iblockstate.getValue(FurnitureHorizontalBlock.DIRECTION) != state.getValue(FurnitureHorizontalBlock.DIRECTION)
				|| iblockstate.getValue(HALF) != state.getValue(HALF);
	}

	public static boolean isBlockStairs(IBlockState state) {
		return Util.getCustomBlock(state) instanceof StairsBlock;
	}

	@Override
	public String getVersionBlockClass() {
		return "StairsVersionBlock";
	}

	public enum EnumHalf implements IStringSerializable {
		TOP,
		BOTTOM;

		@Override
		public String getName() {
			return name().toLowerCase();
		}

	}

	public enum EnumShape implements IStringSerializable {
		STRAIGHT,
		INNER_LEFT,
		INNER_RIGHT,
		OUTER_LEFT,
		OUTER_RIGHT;

		@Override
		public String getName() {
			return name().toLowerCase();
		}

	}

}