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
import net.minecraft.block.state.IBlockState;

public class DefaultHorizontalBlock extends FurnitureHorizontalBlock {
	public DefaultHorizontalBlock(BlockProperties blockProperties) {
		super(blockProperties);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (!openable)
			return super.getStateFromMeta(meta);

		return Util.withProperties(getDefaultState(),
				DIRECTION, Util.getHorizontal(meta & 3),
				OPEN, ((meta & 4) != 0)
		);
	}

	@Override
	public int getMetaFromState(IBlockState blockState) {
		if (!openable)
			return super.getMetaFromState(blockState);

		int meta = super.getMetaFromState(blockState);
		if (blockState.getValue(DefaultHorizontalBlock.OPEN))
			meta |= 0x4;

		return meta;
	}
}
