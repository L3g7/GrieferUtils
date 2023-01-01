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
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.util.EnumFacing.*;

public abstract class ContainerVersionBlock extends VersionBlock implements ITileEntityProvider {

	private final Map<String, IBlockState> validBlockStates;

	public ContainerVersionBlock(ModBlock modBlock) {
		super(modBlock);
		validBlockStates = new HashMap<>();
		isBlockContainer = true;
	}

	protected boolean isInvalidNeighbor(World world, BlockPos pos, EnumFacing facing) {
		return world.getBlockState(pos.offset(facing)).getBlock().getMaterial() == Material.cactus;
	}

	protected boolean hasInvalidNeighbor(World world, BlockPos pos) {
		return isInvalidNeighbor(world, pos, NORTH)
				|| isInvalidNeighbor(world, pos, SOUTH)
				|| isInvalidNeighbor(world, pos, WEST)
				|| isInvalidNeighbor(world, pos, EAST);
	}

	public int getRenderType() {
		return -1;
	}

	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		worldIn.removeTileEntity(pos);
	}

	public boolean onBlockEventReceived(World worldIn, BlockPos pos, IBlockState state, int eventID, int eventParam) {
		super.onBlockEventReceived(worldIn, pos, state, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);

		return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
	}

	@Override
	public Map<String, IBlockState> getValidBlockStates() {
		return validBlockStates;
	}

}
