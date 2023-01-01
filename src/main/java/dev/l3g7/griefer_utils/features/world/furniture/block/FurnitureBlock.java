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

package dev.l3g7.griefer_utils.features.world.furniture.block;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;

import java.util.List;

public class FurnitureBlock extends ModBlock {
	public static final PropertyBool OPEN = PropertyBool.create("open");
	protected boolean openable;

	public FurnitureBlock(BlockProperties blockProperties) {
		super(blockProperties);
	}

	@Override
	public void initBlockData(JsonObject blockData) {
		super.initBlockData(blockData);
		openable = blockData.has("openable") && blockData.get("openable").getAsBoolean();
	}

	@Override
	public void addProperties(List<IProperty<?>> properties) {
		if (openable)
			properties.add(OPEN);
	}

	@Override
	public IBlockState getDefaultState(IBlockState defaultState) {
		if (openable)
			defaultState = defaultState.withProperty(OPEN, false);

		return defaultState;
	}

	@Override
	public boolean isTransparent(IBlockState blockState) {
		return false;
	}

	public boolean isOpenable() {
		return openable;
	}
}
