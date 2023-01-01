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

package dev.l3g7.griefer_utils.features.world.furniture.block.office;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.ModBlock;
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

import static dev.l3g7.griefer_utils.features.world.furniture.block.office.DeskBlock.Type.*;

public class DeskBlock extends FurnitureHorizontalBlock {

	public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);
	private MaterialType materialType;

	public DeskBlock(BlockProperties properties) {
		super(properties);
	}

	@Override
	public void initBlockData(JsonObject blockData) {
		super.initBlockData(blockData);
		materialType = MaterialType.valueOf(blockData.get("type").getAsString());
	}

	@Override
	public IBlockState getDefaultState(IBlockState defaultState) {
		return super.getDefaultState(defaultState).withProperty(TYPE, SINGLE);
	}

	@Override
	public IBlockState getActualState(IBlockState state, BlockPos blockPosition, IBlockAccess world) {
		EnumFacing dir = state.getValue(DIRECTION);
		boolean left = isDesk(world, blockPosition, dir.rotateYCCW(), dir);
		boolean right = isDesk(world, blockPosition, dir.rotateY(), dir);

		if (left && right) return state.withProperty(TYPE, MIDDLE);
		if (left) return state.withProperty(TYPE, RIGHT);
		if (right) return state.withProperty(TYPE, LEFT);
		return state.withProperty(TYPE, SINGLE);
	}

	private boolean isDesk(IBlockAccess world, BlockPos source, EnumFacing checkDirection, EnumFacing tableDirection) {
		IBlockState state = world.getBlockState(source.offset(checkDirection));
		ModBlock block = Util.getCustomBlock(state);
		return block instanceof DeskBlock
				&& ((DeskBlock) block).materialType == materialType
				&& state.getValue(DIRECTION) == tableDirection;
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
		MIDDLE;

		@Override
		public String getName() {
			return name().toLowerCase();
		}

	}

	@SuppressWarnings("unused")
	public enum MaterialType {
		OAK,
		BIRCH,
		SPRUCE,
		JUNGLE,
		ACACIA,
		DARK_OAK,
		STONE,
		GRANITE,
		DIORITE,
		ANDESITE,
		STRIPPED_OAK,
		STRIPPED_BIRCH,
		STRIPPED_SPRUCE,
		STRIPPED_JUNGLE,
		STRIPPED_ACACIA,
		STRIPPED_DARK_OAK
	}

}
