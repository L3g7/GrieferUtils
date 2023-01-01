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

package dev.l3g7.griefer_utils.features.world.furniture.block.multi;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.world.furniture.ModBlocks;
import dev.l3g7.griefer_utils.features.world.furniture.block.ModBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.type.DefaultHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;

public class FridgeBlock extends DefaultHorizontalBlock {
	private ModBlock freezerBlock;

	public FridgeBlock(BlockProperties blockProperties) {
		super(blockProperties);
	}

	@Override
	public void initBlockData(JsonObject blockData) {
		super.initBlockData(blockData);
		openable = true;
		freezerBlock = ModBlocks.getBlockByKey("griefer_utils:" + blockData.getAsJsonPrimitive("freezer").getAsString());
	}

	@Override
	public ModBlock getBlockToSupplyToItem() {
		return freezerBlock;
	}

	public ModBlock getFreezerBlock() {
		return freezerBlock;
	}

}
