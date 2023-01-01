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

import dev.l3g7.griefer_utils.features.world.furniture.block.ReplaceableBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.small.CoffeeTableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class CustomItemBlock extends ItemBlock {

	private final Block mcBlock;
	private final Block suppliedBlock;

	public CustomItemBlock(Block mcBlock, Block suppliedBlock) {
		super(suppliedBlock);
		this.mcBlock = mcBlock;
		this.suppliedBlock = suppliedBlock;
	}

	public int getColorFromItemStack(ItemStack stack, int renderPass) {
		if (!(mcBlock instanceof HedgeVersionBlock))
			return super.getColorFromItemStack(stack, renderPass);

		return mcBlock.getRenderColor(mcBlock.getDefaultState());
	}

	public Block getBlock() {
		return mcBlock;
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!(suppliedBlock instanceof VersionBlock))
			return super.onItemUse(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);

		VersionBlock versionBlock = (VersionBlock)suppliedBlock;
		if (suppliedBlock instanceof CoffeeTableVersionBlock)
			return onItemUseCoffeeTable(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);

		IBlockState iblockstate = worldIn.getBlockState(pos);
		Block block = iblockstate.getBlock();

		if (!block.isReplaceable(worldIn, pos))
			pos = pos.offset(side);

		if (stack.stackSize == 0
			|| !playerIn.canPlayerEdit(pos, side, stack)
			|| !worldIn.canBlockBePlaced(block, pos, false, side, null, stack))
			return false;

		IBlockState state = block.onBlockPlaced(worldIn, pos, side, hitX, hitY, hitZ, getMetadata(stack.getMetadata()), playerIn);
		if (!versionBlock.getModBlock().isValidPosition(state, worldIn, pos))
			return false;

		if (worldIn.setBlockState(pos, state, 3)) {
			state = worldIn.getBlockState(pos);

			if (state.getBlock() == block) {
				setTileEntityNBT(worldIn, playerIn, pos, stack);
				block.onBlockPlacedBy(worldIn, pos, state, playerIn, stack);
			}

			worldIn.playSoundEffect(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0f) / 2.0f, block.stepSound.getFrequency() * 0.8f);
			stack.stackSize--;
		}
		return true;
	}

	private boolean onItemUseCoffeeTable(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		IBlockState iblockstate = worldIn.getBlockState(pos);
		Block block = iblockstate.getBlock();

		if ((!(block instanceof CoffeeTableVersionBlock)
			|| iblockstate.getValue(CoffeeTableBlock.TALL))
			&& !block.isReplaceable(worldIn, pos)){
			pos = pos.offset(side);
			if (worldIn.getBlockState(pos).getBlock() != Blocks.air)
				return false;
		}

		if (stack.stackSize == 0
			|| !playerIn.canPlayerEdit(pos, side, stack))
			return false;

		if (canBlockBePlaced(worldIn, block, pos, side, stack)) {

			IBlockState state = block.onBlockPlaced(worldIn, pos, side, hitX, hitY, hitZ, getMetadata(stack.getMetadata()), playerIn);
			if (worldIn.setBlockState(pos, state, 3)) {
				state = worldIn.getBlockState(pos);

				if (state.getBlock() == block) {
					setTileEntityNBT(worldIn, playerIn, pos, stack);
					block.onBlockPlacedBy(worldIn, pos, state, playerIn, stack);
				}

				worldIn.playSoundEffect(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0f) / 2.0f, block.stepSound.getFrequency() * 0.8f);
				stack.stackSize--;
			}
			return true;
		}

		return false;
	}

	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
		if (!(mcBlock instanceof CoffeeTableVersionBlock))
			return super.canPlaceBlockOnSide(worldIn, pos, side, player, stack);

		IBlockState state = worldIn.getBlockState(pos);
		Block block = state.getBlock();

		if (block == Blocks.snow_layer) {
			side = EnumFacing.UP;
		} else {
			if (stack.getItem() instanceof ItemBlock
				&& block instanceof CoffeeTableVersionBlock
				&& !state.getValue(CoffeeTableBlock.TALL)
				&& !((ItemBlock)stack.getItem()).getBlock().equals(block))
				return false;

			if ((!(block instanceof CoffeeTableVersionBlock)
				|| state.getValue(CoffeeTableBlock.TALL))
				&& !block.isReplaceable(worldIn, pos))
				pos = pos.offset(side);
		}

		return canBlockBePlaced(worldIn, block, pos, side, stack);
	}

	public boolean canBlockBePlaced(World world, Block blockIn, BlockPos pos, EnumFacing side, ItemStack itemStackIn) {
		Block block = world.getBlockState(pos).getBlock();
		AxisAlignedBB bb = blockIn.getCollisionBoundingBox(world, pos, blockIn.getDefaultState());

		return (bb == null
			|| world.checkNoEntityCollision(bb, null))
			&& ((block.getMaterial() == Material.circuits
			&& blockIn == Blocks.anvil)
			|| ((block instanceof ReplaceableBlock
			|| block.getMaterial().isReplaceable())
			&& blockIn.canReplace(world, pos, side, itemStackIn)));
	}

}
