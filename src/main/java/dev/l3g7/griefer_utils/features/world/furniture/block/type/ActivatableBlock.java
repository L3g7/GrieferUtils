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
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;

import java.util.List;

public class ActivatableBlock extends FurnitureHorizontalBlock {

	public static final PropertyBool ACTIVATED = PropertyBool.create("activated");

	public ActivatableBlock(BlockProperties blockProperties) {
		super(blockProperties);
	}

	@Override
	public void addProperties(List<IProperty<?>> properties) {
		super.addProperties(properties);
		properties.add(ActivatableBlock.ACTIVATED);
	}

	@Override
	public IBlockState getDefaultState(IBlockState defaultState) {
		return super.getDefaultState(defaultState).withProperty(ActivatableBlock.ACTIVATED, false);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		boolean activated = meta > 3;
		if (meta > 3)
			meta -= 4;

		return super.getStateFromMeta(meta).withProperty(ActivatableBlock.ACTIVATED, activated);
	}

	@Override
	public int getMetaFromState(IBlockState blockState) {
		int meta = super.getMetaFromState(blockState);
		if (blockState.getValue(ActivatableBlock.ACTIVATED))
			meta += 4;

		return meta;
	}

}
