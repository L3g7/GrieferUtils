/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.features.modules;


import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.SchematicaUtil;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.utils.Material;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK;

@Singleton
public class BlockInfo extends Module {

	private static final Pair<BlockPos, ItemStack> DEFAULT_DATA = Pair.of(new BlockPos(0, 0, 0), new ItemStack(Blocks.command_block));
	public static boolean gettingTooltip = false;

	private List<String> lines = new ArrayList<>();
	private Pair<BlockPos, ItemStack> data = null;

	private final BooleanSetting showCoords = new BooleanSetting()
		.name("Koordinaten anzeigen")
		.description("Ob die Koordinaten des anvisierten Blocks auch angezeigt werden sollen.")
		.icon(Material.COMPASS);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Block-Infos")
		.description("Zeigt dir Infos den anvisierten Block an.\n\nFunktioniert auch mit Schematica.")
		.icon("magnifying_glass")
		.subSettings(showCoords);

	@Override
	public String[] getValues() {
		return new String[] {""};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[] {""};
	}

	@Override
	public double getRawWidth() {
		int maxLength = 0;
		for (String line : lines)
			maxLength = Math.max(maxLength, mc().fontRendererObj.getStringWidth(line));

		return 35 + maxLength;
	}

	@Override
	public double getRawHeight() {
		return showCoords.get() ? 40 : 30;
	}

	@Override
	public boolean isShown() {
		return true;
	}

	@Override
	public void draw(double x, double y, double rightX) {
		updateObjectMouseOver();

		if (data == null || data.getRight() == null || data.getRight().getItem() == null)
			return;

		gettingTooltip = true; // Prevents ItemInfo from triggering
		lines = data.getRight().getTooltip(player(), true);
		gettingTooltip = false;

		if (showCoords.get())
			lines.add(String.format("§8X: %d Y: %d Z: %d", data.getLeft().getX(), data.getLeft().getY(), data.getLeft().getZ()));

		RenderUtil.renderToolTipWithPadding(lines, (int) x - 7, (int) y + 15, Integer.MAX_VALUE, Integer.MAX_VALUE, 25, 2, 0, (a, b) -> {
			GlStateManager.pushMatrix();
			GlStateManager.scale(1.5, 1.5, 1.5);
			drawUtils().drawItem(data.getRight(), a / 1.5 + 0.5, b / 1.5 + (showCoords.get() ? 2  : -2), null);
			GlStateManager.popMatrix();
		});
		GlStateManager.disableLighting();
	}

	private void updateObjectMouseOver() {
		if (world() == null) {
			data = mc.currentScreen instanceof LabyModModuleEditorGui ? DEFAULT_DATA : null;
			return;
		}

		if (Constants.SCHEMATICA)
			if (updateObjectMouseOverFromSchematica())
				return;

		MovingObjectPosition mop = mc().objectMouseOver;
		if (mop == null || mop.typeOfHit != BLOCK) {
			data = mc.currentScreen instanceof LabyModModuleEditorGui ? DEFAULT_DATA : null;
			return;
		}

		IBlockState state = world().getBlockState(mop.getBlockPos());
		ItemStack pickedStack = getPickBlock(state.getBlock(), world(), mop.getBlockPos());
		if (state.getBlock() instanceof BlockSkull) {
			TileEntitySkull tes = (TileEntitySkull) world().getTileEntity(mop.getBlockPos());
			pickedStack.setItemDamage(tes.getSkullType());
			if (tes.getSkullType() == 3 && tes.getPlayerProfile() != null) {
				NBTTagCompound tag = new NBTTagCompound();
				NBTTagCompound nbttagcompound = new NBTTagCompound();

				NBTUtil.writeGameProfile(nbttagcompound, tes.getPlayerProfile());
				tag.setTag("SkullOwner", nbttagcompound);
				pickedStack.setTagCompound(tag);
			}
		}

		data = Pair.of(mop.getBlockPos(), pickedStack == null ? new ItemStack(state.getBlock()) : pickedStack);
	}

	private boolean updateObjectMouseOverFromSchematica() {
		if (SchematicaUtil.dontRender())
			return false;

		MovingObjectPosition mop = SchematicaUtil.getMovingObjectPosition();
		if (mop == null || mop.typeOfHit != BLOCK)
			return false;

		IBlockState state = SchematicaUtil.getWorld().getBlockState(mop.getBlockPos());
		ItemStack pickedStack = getPickBlock(state.getBlock(),  SchematicaUtil.getWorld(), mop.getBlockPos());
		ItemStack stack = pickedStack == null ? new ItemStack(state.getBlock()) : pickedStack;
		stack.setStackDisplayName("§r" + stack.getDisplayName() + " §b(S)");

		data = Pair.of(mop.getBlockPos(), stack);
		return true;
	}

}
