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

package dev.l3g7.griefer_utils.features.world.furniture.block.version_specific;

import dev.l3g7.griefer_utils.features.world.furniture.block.ModBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class TallGrassVersionBlock extends PlantyVersionBlock {

	public TallGrassVersionBlock(ModBlock modBlock) {
		super(modBlock);
	}

	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		BlockProperties properties = modBlock.getBlockProperties();

		if (properties.hardness == 0 && properties.resistance == 0)
			return rand.nextInt(8) == 0 ? Items.wheat_seeds : null;

		return Item.getItemFromBlock(this);
	}

	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
		if (worldIn.isRemote || player.getCurrentEquippedItem() == null || player.getCurrentEquippedItem().getItem() != Items.shears) {
			super.harvestBlock(worldIn, player, pos, state, te);
			return;
		}

		player.triggerAchievement(StatList.mineBlockStatArray[getIdFromBlock(this)]);
		spawnAsEntity(worldIn, pos, new ItemStack(Item.getItemFromBlock(this), 1));
	}

}
