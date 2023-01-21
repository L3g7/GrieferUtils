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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import static dev.l3g7.griefer_utils.features.world.furniture.block.general.StairsBlock.EnumHalf.BOTTOM;
import static dev.l3g7.griefer_utils.features.world.furniture.block.general.StairsBlock.EnumHalf.TOP;
import static dev.l3g7.griefer_utils.features.world.furniture.block.general.StairsBlock.EnumShape.STRAIGHT;
import static dev.l3g7.griefer_utils.features.world.furniture.block.general.StairsBlock.HALF;
import static dev.l3g7.griefer_utils.features.world.furniture.block.general.StairsBlock.SHAPE;
import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.UP;

public class StairsVersionBlock extends VersionBlock {

	public StairsVersionBlock(ModBlock modBlock) {
		super(modBlock);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		IBlockState iblockstate = super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(SHAPE, STRAIGHT);
		return iblockstate.withProperty(HALF, facing != DOWN && (facing == UP || hitY <= 0.5) ? BOTTOM : TOP);
	}

}
