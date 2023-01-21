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

package dev.l3g7.griefer_utils.features.world.furniture.block.version_specific;

import dev.l3g7.griefer_utils.features.world.furniture.block.ModBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.multi.FreezerBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.multi.FridgeBlock;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class FridgeVersionBlock extends VersionBlock {

	public FridgeVersionBlock(ModBlock modBlock) {
		super(modBlock);
	}

	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		IBlockState downState = worldIn.getBlockState(pos.down());

		if (Util.getCustomBlock(downState) instanceof FreezerBlock) {
			worldIn.setBlockState(pos.down(), Blocks.air.getDefaultState(), 35);
			worldIn.playAuxSFXAtEntity(player, 2001, pos.down(), getStateId(downState));
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	public Item getItem(World worldIn, BlockPos pos) {
		return Item.getItemFromBlock(((FridgeBlock) getModBlock()).getFreezerBlock().getBlockHandle());
	}

}
