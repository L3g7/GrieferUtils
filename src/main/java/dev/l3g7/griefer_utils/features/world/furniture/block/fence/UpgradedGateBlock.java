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

package dev.l3g7.griefer_utils.features.world.furniture.block.fence;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import java.util.List;

import static dev.l3g7.griefer_utils.features.world.furniture.block.fence.UpgradedGateBlock.DoorHingeSide.LEFT;
import static dev.l3g7.griefer_utils.features.world.furniture.block.fence.UpgradedGateBlock.DoorHingeSide.RIGHT;

public class UpgradedGateBlock extends FurnitureHorizontalBlock {

	public static final PropertyEnum<DoorHingeSide> HINGE = PropertyEnum.create("hinge", DoorHingeSide.class);
	public static final PropertyBool DOUBLE = PropertyBool.create("double");

	public UpgradedGateBlock(BlockProperties blockProperties) {
		super(blockProperties);
	}

	@Override
	public void initBlockData(JsonObject blockData) {
		super.initBlockData(blockData);
		openable = true;
	}

	@Override
	public int getMetaFromState(IBlockState blockState) {
		int meta = blockState.getValue(DIRECTION).ordinal() - 2;

		if (blockState.getValue(OPEN))
			meta |= 4;
		if (blockState.getValue(HINGE) == RIGHT)
			meta |= 8;
		if (blockState.getValue(DOUBLE))
			meta |= 16;

		return meta;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		DoorHingeSide doorHingeSide = (meta & 4) == 0 ? LEFT : RIGHT;
		boolean open = (meta & 8) != 0;
		boolean isDouble = (meta & 16) != 0;

		return Util.withProperties(getDefaultState(),
				DIRECTION, Util.getHorizontal(meta & 3),
				HINGE, doorHingeSide,
				OPEN, open,
				DOUBLE, isDouble
		);
	}

	@Override
	public IBlockState getDefaultState(IBlockState defaultState) {
		return Util.withProperties(super.getDefaultState(defaultState),
				HINGE, LEFT,
				DOUBLE, false
		);
	}

	@Override
	public void addProperties(List<IProperty<?>> properties) {
		super.addProperties(properties);
		properties.add(HINGE);
		properties.add(DOUBLE);
	}

	public enum DoorHingeSide implements IStringSerializable {
		LEFT,
		RIGHT;

		@Override
		public String getName() {
			return name().toLowerCase();
		}

	}

}