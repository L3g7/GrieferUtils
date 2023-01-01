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
import dev.l3g7.griefer_utils.features.world.furniture.block.ReplaceableBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import static dev.l3g7.griefer_utils.features.world.furniture.block.small.CoffeeTableBlock.TALL;

public class CoffeeTableVersionBlock extends VersionBlock implements ReplaceableBlock {

	public CoffeeTableVersionBlock(ModBlock modBlock) {
		super(modBlock);
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		IBlockState state = worldIn.getBlockState(pos);

		return state.getBlock() == this
				? state.withProperty(TALL, true)
				: super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
	}

	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
		if (worldIn.isRemote)
			return;

		for (int quantity = state.getValue(TALL) ? 2 : 1, j = 0; j < quantity; ++j) {
			if (worldIn.rand.nextFloat() <= chance) {
				Item item = getItemDropped(state, worldIn.rand, fortune);
				if (item != null)
					spawnAsEntity(worldIn, pos, new ItemStack(item, 1, damageDropped(state)));
			}
		}
	}

}
