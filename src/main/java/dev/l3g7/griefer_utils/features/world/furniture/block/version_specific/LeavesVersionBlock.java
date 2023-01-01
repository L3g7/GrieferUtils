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

import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.features.world.furniture.ModBlocks;
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

import java.util.Map;
import java.util.Random;

public class LeavesVersionBlock extends VersionBlock {

	private static final Map<String, String> BLOCK_TO_ITEM = ImmutableMap.of(
			"tile.white", "pink_sapling01",
			"tile.pink", "pink_sapling01",
			"tile.purple", "purple_sapling01",
			"tile.blue", "blue_sapling01"
	);

	public LeavesVersionBlock(ModBlock modBlock) {
		super(modBlock);
	}

	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		BlockProperties properties = modBlock.getBlockProperties();
		if (properties.hardness == 0.2f && properties.resistance == 1)
			return getItemFromBlock();

		return super.getItemDropped(state, rand, fortune);
	}

	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
		if (worldIn.isRemote || player.getCurrentEquippedItem() == null || player.getCurrentEquippedItem().getItem() != Items.shears) {
			super.harvestBlock(worldIn, player, pos, state, te);
			return;
		}

		player.triggerAchievement(StatList.mineBlockStatArray[getIdFromBlock(this)]);
		spawnAsEntity(worldIn, pos, new ItemStack(Item.getItemFromBlock(this), 1));
	}

	private Item getItemFromBlock() {
		for (Map.Entry<String, String> entry : BLOCK_TO_ITEM.entrySet())
			if (getUnlocalizedName().startsWith(entry.getKey()))
				return Item.getItemFromBlock(ModBlocks.getBlockByKey("griefer_utils:" + entry.getValue()).getBlockHandle());

		return Item.getItemFromBlock(this);
	}

	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
		if (worldIn.isRemote)
			return;

		int dropChance = 20;
		if (fortune > 0)
			dropChance = Math.min(dropChance - 2 << fortune, 10);

		if (worldIn.rand.nextInt(dropChance) != 0)
			return;

		Item item = getItemDropped(state, worldIn.rand, fortune);
		spawnAsEntity(worldIn, pos, new ItemStack(item, 1, damageDropped(state)));
	}

	@Override
	public boolean isFullCube() {
		return true;
	}

}
