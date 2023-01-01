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

package dev.l3g7.griefer_utils.features.world.furniture;

import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;

public class OptifineBlockAccess implements IBlockAccess {
	private final IBlockAccess chunkCache;

	public OptifineBlockAccess(IBlockAccess chunkCache) {
		this.chunkCache = chunkCache;
	}

	public IBlockState getBlockState(BlockPos pos) {
		return Reflection.invoke(chunkCache, "getBlockState", pos);
	}

	public TileEntity getTileEntity(BlockPos pos) { return null; }
	public int getCombinedLight(BlockPos pos, int lightValue) { return 0; }
	public boolean isAirBlock(BlockPos pos) { return false; }
	public BiomeGenBase getBiomeGenForCoords(BlockPos pos) { return null; }
	public boolean extendedLevelsInChunkCache() { return false; }
	public int getStrongPower(BlockPos pos, EnumFacing direction) { return 0; }
	public WorldType getWorldType() { return null; }
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) { return false; }

}
