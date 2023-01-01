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
import dev.l3g7.griefer_utils.features.world.furniture.block.beacon.BeaconBlock;
import dev.l3g7.griefer_utils.features.world.furniture.util.BeaconVersionColor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public class BeaconVersionBlock extends ContainerVersionBlock {
	private long startTime;
	private float[] currentColor;
	private long fullCycle;
	private float oneCycle;
	private final BeaconVersionColor versionColor;

	public BeaconVersionBlock(ModBlock modBlock) {
		super(modBlock);
		fullCycle = 1000L;
		oneCycle = 1000.0f;
		versionColor = BeaconVersionColor.of(((BeaconBlock) modBlock).getBeaconColor());

		if (!versionColor.isRainbow())
			return;

		startTime = System.currentTimeMillis();
		fullCycle = 16000L;
		oneCycle = fullCycle / (float) EnumDyeColor.values().length;
	}

	public void updateColor() {
		long elapsed = System.currentTimeMillis() - startTime;
		long progress = elapsed / fullCycle;
		if (progress >= 1L)
			startTime = System.currentTimeMillis();

		elapsed = System.currentTimeMillis() - startTime;
		int colorFromIndex = (int) (elapsed / oneCycle);
		float progressCycle = elapsed % oneCycle / oneCycle;
		int colorToIndex = colorFromIndex + 1;
		if (colorToIndex >= EnumDyeColor.values().length)
			colorToIndex = 0;

		float[] colorFrom = EntitySheep.getDyeRgb(EnumDyeColor.values()[colorFromIndex]);
		float[] colorTo = EntitySheep.getDyeRgb(EnumDyeColor.values()[colorToIndex]);

		float r = (1.0f - progressCycle) * colorFrom[0] + progressCycle * colorTo[0];
		float g = (1.0f - progressCycle) * colorFrom[1] + progressCycle * colorTo[1];
		float b = (1.0f - progressCycle) * colorFrom[2] + progressCycle * colorTo[2];
		currentColor = new float[]{r, g, b};
	}

	public float[] getColor() {
		if (!(modBlock instanceof BeaconBlock))
			return EntitySheep.getDyeRgb(EnumDyeColor.CYAN);

		if (versionColor.isRainbow())
			return currentColor;

		return versionColor.getRgbColor();
	}

	private float[] getCustomRgb(int r, int g, int b) {
		return new float[] { r / 255f, g / 255f, b / 255f };
	}

	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityBeacon();
	}

	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			return true;

		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileEntityBeacon) {
			playerIn.displayGUIChest((IInventory) tileentity);
			playerIn.triggerAchievement(StatList.field_181730_N);
		}

		return true;
	}

	@Override
	public int getRenderType() {
		return 3;
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

		if (!stack.hasDisplayName())
			return;

		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileEntityBeacon)
			((TileEntityBeacon) tileentity).setName(stack.getDisplayName());
	}

	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (!(tileentity instanceof TileEntityBeacon))
			return;

		((TileEntityBeacon) tileentity).updateBeacon();
		worldIn.addBlockEvent(pos, this, 1, 0);
	}

	@Override
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	public static void updateColorAsync(World worldIn, BlockPos glassPos) {
		HttpUtil.field_180193_a.submit(() -> {
			Chunk chunk = worldIn.getChunkFromBlockCoords(glassPos);
			for (int i = glassPos.getY() - 1; i >= 0; --i) {
				BlockPos blockpos = new BlockPos(glassPos.getX(), i, glassPos.getZ());
				if (!chunk.canSeeSky(blockpos))
					break;

				IBlockState iblockstate = worldIn.getBlockState(blockpos);
				if (iblockstate.getBlock() != Blocks.beacon)
					return;

				((WorldServer) worldIn).addScheduledTask(() -> {
					TileEntity tileentity = worldIn.getTileEntity(blockpos);
					if (!(tileentity instanceof TileEntityBeacon))
						return;

					((TileEntityBeacon) tileentity).updateBeacon();
					worldIn.addBlockEvent(blockpos, Blocks.beacon, 1, 0);
				});
			}
		});
	}

}