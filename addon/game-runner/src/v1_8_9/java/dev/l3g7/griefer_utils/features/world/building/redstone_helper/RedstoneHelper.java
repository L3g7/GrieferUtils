/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building.redstone_helper;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.render.block_overlay.BlockOverlayCollector;
import dev.l3g7.griefer_utils.core.util.render.block_overlay.BlockOverlayCollector.RenderObject;
import dev.l3g7.griefer_utils.core.util.render.block_overlay.BlockOverlayCollector.RenderObjectGenerator;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.world.building.redstone_helper.FontRenderData.CharRenderInfo;
import dev.l3g7.griefer_utils.features.world.building.redstone_helper.FontRenderData.Vertices;
import dev.l3g7.griefer_utils.features.world.building.redstone_helper.render_pipeline.RenderChunk;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.function.Supplier;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static net.minecraft.init.Blocks.*;

/**
 * Implements cursor movement, selection and copy and paste in the sign edit gui.
 */
@Singleton
public class RedstoneHelper extends Feature implements RenderObjectGenerator {

	private static final SwitchSetting showZeroPower = SwitchSetting.create()
		.name("0 anzeigen")
		.description("Ob die Stärke-Anzeige auch angezeigt werden soll, wenn die Stärke 0 beträgt.")
		.icon("redstone_repeater")
		.callback(RenderChunk::onSettingsChange);

	private static final SwitchSetting showPower = SwitchSetting.create()
		.name("Redstone-Stärke anzeigen")
		.description("Zeigt auf Redstone-Kabeln ihre derzeitige Stärke an.")
		.icon("redstone")
		.subSettings(showZeroPower)
		.callback(RenderChunk::onSettingsChange);

	private static final SwitchSetting showDirection = SwitchSetting.create()
		.name("Richtung anzeigen")
		.description("Zeigt die Richtung von Werfern / Spendern und Trichtern.")
		.icon("axes")
		.callback(RenderChunk::onSettingsChange);

	private static final SwitchSetting showCauldronLevel = SwitchSetting.create()
		.name("Kessel-Füllstand anzeigen")
		.description("Zeigt an einem Kessel seinen derzeitigen Füllstand an.")
		.icon("water_breathing");

	public static final SwitchSetting hideRedstoneParticles = SwitchSetting.create()
		.name("Redstone-Partikel verstecken")
		.description("Versteckt die Partikel, die durch aktivertes Redstone erzeugt werden.")
		.icon("redstone_particles")
		.defaultValue(true);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Redstone-Helfer")
		.description("Hilft beim Arbeiten mit Redstone.")
		.icon("redstone_comparator")
		.subSettings(showPower, showDirection, showCauldronLevel, HeaderSetting.create(), hideRedstoneParticles)
		.callback(RenderChunk::onSettingsChange);

	public static RedstoneHelper get() {
		return get(RedstoneHelper.class);
	}

	@Override
	public void init() {
		super.init();
		BlockOverlayCollector.registerRenderObjectGenerator(this);
	}

	@Override
	public RenderObject getRenderObject(IBlockState state, BlockPos pos, WorldClient world) {
		Block block = state.getBlock();
		if (block == cauldron)
			return new TextRRO(showCauldronLevel, () -> state.getValue(BlockCauldron.LEVEL));

		if (block == hopper) {
			EnumFacing facing = state.getValue(BlockHopper.FACING);
			return facing.getHorizontalIndex() == -1 ? null : new Hopper(facing);
		}

		if (block != dropper && block != dispenser)
			return null;

		EnumFacing dir = state.getValue(BlockDispenser.FACING);
		return new Dropper(dir);
	}

	@Mixin(BlockRedstoneWire.class)
	private static class MixinBlockRedstoneWire {

		@Inject(method = "randomDisplayTick", at = @At("HEAD"), cancellable = true)
		private void injectRandomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
			if (RedstoneHelper.get().isEnabled() && hideRedstoneParticles.get())
				ci.cancel();
		}

	}

	public static class Wire extends dev.l3g7.griefer_utils.features.world.building.redstone_helper.RenderObject {

		public final int power;

		public Wire(BlockPos pos, int power) {
			super(pos);
			this.power = power;
		}

		public double getYOffset() { return 0.02f; }

		@Override
		public CharRenderInfo[] getRenderInfos() {
			return FontRenderData.getCharRenderInfos(String.valueOf(power));
		}

		@Override
		public Vertices getVertices(CharRenderInfo charRenderInfo, int rotation) {
			return charRenderInfo.rotatedVertices()[rotation];
		}

		@Override
		public boolean equals(dev.l3g7.griefer_utils.features.world.building.redstone_helper.RenderObject r) {
			return super.equals(r) && power == ((Wire) r).power;
		}

	}

	public static class Hopper extends RenderObject {

		private final EnumFacing dir;

		private Hopper(EnumFacing dir) {
			this.dir = dir;
		}

		public void render(BlockPos pos, float partialTicks) {
			if (!showDirection.get())
				return;

			prepareRender(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), partialTicks);

			switch (dir) {
				case NORTH:
					GlStateManager.rotate(90, 0, -1, 0);
					break;
				case SOUTH:
					GlStateManager.rotate(90, 0, 1, 0);
					break;
				case EAST:
					GlStateManager.rotate(180, 0, 1, 0);
					break;
				case WEST:
					break;
			}
			GlStateManager.scale(0.1, 0.1, 0.1);
			GlStateManager.translate(-3.5, 7.01, -5.25);

			GlStateManager.rotate(90, 1, 0, 0);
			mc().fontRendererObj.drawString("⬅", 0, 0, 0xFFFFFF);

			GlStateManager.popMatrix();
		}

	}

	private static class Dropper extends RenderObject {

		private final EnumFacing dir;

		private Dropper(EnumFacing dir) {
			this.dir = dir;
		}

		public void render(BlockPos pos, float partialTicks) {
			if (!showDirection.get())
				return;

			prepareRender(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), partialTicks);

			switch (dir) {
				case UP:
					GlStateManager.rotate(270, 0, 0, 1);
					GlStateManager.translate(-0.51, -0.51, 0);
					break;
				case DOWN:
					GlStateManager.rotate(270, 0, 0, -1);
					GlStateManager.translate(0.51, -0.51, 0);
					break;
				case NORTH:
					GlStateManager.rotate(90, 0, -1, 0);
					break;
				case SOUTH:
					GlStateManager.rotate(90, 0, 1, 0);
					break;
				case EAST:
					GlStateManager.rotate(180, 0, 1, 0);
					break;
				case WEST:
					break;
			}
			GlStateManager.translate(-0.35, 0, -0.51);

			GlStateManager.scale(0.1, 0.1, 0.1);

			mc().fontRendererObj.drawString("⬅", 0, 0, 0);

			GlStateManager.translate(0, 0, 10.2);

			mc().fontRendererObj.drawString("⬅", 0, 0, 0);
			GlStateManager.translate(0, 10.2, -10.35);

			GlStateManager.rotate(90, 1, 0, 0);
			mc().fontRendererObj.drawString("⬅", 0, 0, 0);

			GlStateManager.translate(0, 0, 10.5);
			mc().fontRendererObj.drawString("⬅", 0, 0, 0);

			GlStateManager.popMatrix();
		}
	}

	private static class TextRRO extends RenderObject {

		private final SwitchSetting setting;
		private final Supplier<Object> textSupplier;

		private TextRRO(SwitchSetting setting, Supplier<Object> textSupplier) {
			this.textSupplier = textSupplier;
			this.setting = setting;
		}

		@Override
		public void render(BlockPos pos, float partialTicks) {
			if (!setting.get())
				return;

			prepareRender(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), partialTicks);

			GlStateManager.translate(-0.0175, 0.63, -0.51);
			GlStateManager.scale(-0.035, -0.035, 0.035);

			String text = String.valueOf(textSupplier.get());
			int x = -mc().fontRendererObj.getStringWidth(text) / 2;

			mc().fontRendererObj.drawString(text, x, 0, 0xFFFFFF);

			GlStateManager.translate(-1, 0, 29);
			GlStateManager.scale(-1, 1, 1);

			mc().fontRendererObj.drawString(text, x, 0, 0xFFFFFF);
			GlStateManager.translate(14, 0, -15);

			GlStateManager.rotate(90, 0, 1, 0);
			mc().fontRendererObj.drawString(text, x, 0, 0xFFFFFF);

			GlStateManager.translate(-1, 0, -29);
			GlStateManager.scale(-1, 1, 1);
			mc().fontRendererObj.drawString(text, x, 0, 0xFFFFFF);

			GlStateManager.scale(-1, 1, 1);
			GlStateManager.translate(4, -11, 15);

			GlStateManager.rotate(90, 1, 0, 0);
			GlStateManager.rotate(90, 0, 0, 1);
			mc().fontRendererObj.drawString(text, x, 0, 0xFFFFFF);

			GlStateManager.translate(0, 7.1, -29.1);
			GlStateManager.scale(1, -1, 1);

			mc().fontRendererObj.drawString(text, x, 0, 0xFFFFFF);

			GlStateManager.popMatrix();
		}

	}

}
