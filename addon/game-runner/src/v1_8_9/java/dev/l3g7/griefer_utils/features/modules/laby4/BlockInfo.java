/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules.laby4;


import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.modules.Laby4Module;
import dev.l3g7.griefer_utils.features.modules.TempBlockInfoBridge;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.util.SchematicaUtil;
import dev.l3g7.griefer_utils.core.util.render.RenderUtil;
import net.labymod.api.client.gui.hud.HudWidgetRendererAccessor;
import net.labymod.api.client.gui.hud.binding.dropzone.HudWidgetDropzone;
import net.labymod.api.client.gui.hud.binding.dropzone.NamedHudWidgetDropzones;
import net.labymod.api.client.gui.hud.hudwidget.HudWidget;
import net.labymod.api.client.gui.hud.position.HudSize;
import net.labymod.api.client.gui.hud.position.HudWidgetAnchor;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.render.matrix.Stack;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK;

@Bridge
@Singleton
public class BlockInfo extends Laby4Module implements TempBlockInfoBridge {

	private static final Pair<BlockPos, ItemStack> DEFAULT_DATA = Pair.of(new BlockPos(0, 0, 0), new ItemStack(Blocks.command_block));
	public static boolean gettingTooltip = false;

	private List<String> lines = new ArrayList<>();
	private Pair<BlockPos, ItemStack> data = null;

	private final SwitchSetting showCoords = SwitchSetting.create()
		.name("Koordinaten anzeigen")
		.description("Ob die Koordinaten des anvisierten Blocks auch angezeigt werden sollen.")
		.icon(Items.compass);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Block-Infos")
		.description("Zeigt dir Infos des anvisierten Block an.\n\nFunktioniert auch mit Schematica.")
		.icon("magnifying_glass")
		.subSettings(showCoords);

	public BlockInfo() {
		bindDropzones(new BlockInfoDropzone());
	}

	@Override
	public void render(Stack stack, MutableMouse mouse, float partialTicks, boolean isEditorContext, HudSize size) {
		size.set(getWidth(), showCoords.get() ? 40 : 30);
		if (stack != null) {
			this.renderEntireBackground(stack, size);
			draw(0, 0, isEditorContext);
		}
	}

	@Override
	public void load(ModuleConfig config) {
		super.load(config);
		getSettings().remove(0);
	}

	private float getWidth() {
		int maxLength = 0;
		for (String line : lines)
			maxLength = Math.max(maxLength, mc().fontRendererObj.getStringWidth(line));

		return 35 + maxLength;
	}

	private void draw(double x, double y, boolean isEditorContext) {
		updateObjectMouseOver(isEditorContext);

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
			DrawUtils.drawItem(data.getRight(), a / 1.5 + 0.5, b / 1.5 + (showCoords.get() ? 2 : -2), null);
			GlStateManager.popMatrix();
		});
		GlStateManager.disableLighting();
	}

	private void updateObjectMouseOver(boolean isEditorContext) {
		if (world() == null) {
			data = isEditorContext ? DEFAULT_DATA : null;
			return;
		}

		if (Constants.SCHEMATICA)
			if (updateObjectMouseOverFromSchematica())
				return;

		MovingObjectPosition mop = mc().objectMouseOver;
		if (mop == null || mop.typeOfHit != BLOCK) {
			data = isEditorContext ? DEFAULT_DATA : null;
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
		ItemStack pickedStack = getPickBlock(state.getBlock(), SchematicaUtil.getWorld(), mop.getBlockPos());
		ItemStack stack = pickedStack == null ? new ItemStack(state.getBlock()) : pickedStack;
		stack.setStackDisplayName("§r" + stack.getDisplayName() + " §b(S)");

		data = Pair.of(mop.getBlockPos(), stack);
		return true;
	}

	@Override
	public boolean gettingTooltip() {
		return gettingTooltip;
	}

	private static class BlockInfoDropzone extends HudWidgetDropzone {

		public BlockInfoDropzone() {
			super("griefer_utils_block_info");
		}

		public float getX(HudWidgetRendererAccessor renderer, HudSize hudWidgetSize) {
			return renderer.getArea().getCenterX() - hudWidgetSize.getScaledWidth() / 2;
		}

		public float getY(HudWidgetRendererAccessor renderer, HudSize hudWidgetSize) {
			float offset = 0.0f;

			HudWidget<?> bossBar = renderer.getRelevantHudWidgetForDropzone(NamedHudWidgetDropzones.BOSS_BAR);
			if (bossBar != null)
				offset += renderer.getWidget(bossBar).scaledBounds().getHeight();

			HudWidget<?> direction = renderer.getRelevantHudWidgetForDropzone(NamedHudWidgetDropzones.DIRECTION);
			if (direction != null)
				offset += renderer.getWidget(direction).scaledBounds().getHeight() + 4;
			else if (bossBar != null)
				offset += 2;

			return renderer.getArea().getTop() + offset + 1;
		}

		public HudWidgetDropzone copy() {
			return new BlockInfoDropzone();
		}

		public HudWidgetAnchor getAnchor() {
			return HudWidgetAnchor.CENTER_TOP;
		}
	}

}
