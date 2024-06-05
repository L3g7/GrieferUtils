/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.world;

import com.google.common.base.Strings;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.NoteBlockPlayEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.world.redstone_helper.RenderObjectObserver;
import dev.l3g7.griefer_utils.v1_8_9.features.world.redstone_helper.VertexDataStorage;
import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.util.render.WorldBlockOverlayRenderer;
import dev.l3g7.griefer_utils.core.util.render.WorldBlockOverlayRenderer.RenderObject;
import dev.l3g7.griefer_utils.core.util.render.WorldBlockOverlayRenderer.RenderObjectGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
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
		.icon(Items.redstone)
		.callback(RenderObjectObserver.Chunk::onSettingsChange);

	private static final SwitchSetting showPower = SwitchSetting.create()
		.name("Redstone-Stärke anzeigen")
		.description("Zeigt auf Redstone-Kabeln ihre derzeitige Stärke an.")
		.icon(Items.redstone)
		.subSettings(showZeroPower)
		.callback(RenderObjectObserver.Chunk::onSettingsChange);

	private static final SwitchSetting showDirection = SwitchSetting.create()
		.name("Richtung anzeigen")
		.description("Zeigt die Richtung von Werfern / Spendern und Trichtern.")
		.icon(Items.compass)
		.callback(RenderObjectObserver.Chunk::onSettingsChange);

	private static final SwitchSetting showNoteId = SwitchSetting.create()
		.name("Ton-ID anzeigen")
		.description("Ob der Name des Tons oder die ID angezeigt werden soll.")
		.icon(noteblock)
		.defaultValue(true);

	private static final SwitchSetting showNoteBlockPitch = SwitchSetting.create()
		.name("Notenblock-Höhe anzeigen")
		.description("Zeigt an, welche Tonhöhe bei Notenblöcken eingestellt ist."
			+ "\nDafür muss von diesem Block ein Ton abgespielt worden sein.")
		.icon(noteblock)
		.subSettings(showNoteId);

	private static final SwitchSetting showCauldronLevel = SwitchSetting.create()
		.name("Kessel-Füllstand anzeigen")
		.description("Zeigt an einem Kessel seinen derzeitigen Füllstand an.")
		.icon(Items.cauldron);

	private static final NumberSetting range = NumberSetting.create()
		.name("Radius")
		.description("Der Radius um den Spieler in Chunks, in dem die Informationen angezeigt werden."
			+ "\n(-1 ist unendlich)")
		.defaultValue(-1)
		.min(-1)
		.icon(Items.compass);

	public static final SwitchSetting hideRedstoneParticles = SwitchSetting.create()
		.name("Redstone-Partikel verstecken")
		.description("Versteckt die Partikel, die durch aktivertes Redstone erzeugt werden.")
		.icon(Items.redstone)
		.defaultValue(true);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Redstone-Helfer")
		.description("Hilft beim Arbeiten mit Redstone.")
		.icon(Items.redstone)
		.subSettings(showPower, showDirection, showNoteBlockPitch, showCauldronLevel, range, HeaderSetting.create(), hideRedstoneParticles)
		.callback(RenderObjectObserver.Chunk::onSettingsChange);

	@Override
	public void init() {
		super.init();
		WorldBlockOverlayRenderer.registerRenderObjectGenerator(this);
	}

	@Override
	public RenderObject getRenderObject(IBlockState state, BlockPos pos, WorldClient world) {
		Block block = state.getBlock();
		if (block == cauldron)
			return new TextRRO(showCauldronLevel, () -> state.getValue(BlockCauldron.LEVEL));

		if (block != dropper && block != dispenser)
			return null;

		EnumFacing dir = state.getValue(BlockDispenser.FACING);
		return new Dropper(dir);
	}

	@Override
	public void onBlockUpdate(Map<BlockPos, RenderObject> map, BlockPos pos, IBlockState state) {
		if (state.getBlock() != noteblock || !showNoteBlockPitch.get())
			map.remove(pos);
	}

	@EventListener
	private void onNoteBlock(NoteBlockPlayEvent event) {
		BlockPos pos = event.pos;
		ChunkCoordIntPair pair = new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4);
		Map<BlockPos, RenderObject> map = WorldBlockOverlayRenderer.getRenderObjectsForChunk(pair);
		String name = event.getNote().name().replace("_SHARP", "is");
		name += Strings.repeat("'", event.getOctave().ordinal() + 1);
		String finalName = name;
		map.put(pos, new TextRRO(showNoteBlockPitch, () -> showNoteId.get() ? String.valueOf(event.getVanillaNoteId()) : finalName));
	}

	@Mixin(BlockRedstoneWire.class)
	private static class MixinBlockRedstoneWire {

		@Inject(method = "randomDisplayTick", at = @At("HEAD"), cancellable = true)
		private void injectRandomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
			if (FileProvider.getSingleton(RedstoneHelper.class).isEnabled() && hideRedstoneParticles.get())
				ci.cancel();
		}

	}

	public static class Wire extends dev.l3g7.griefer_utils.v1_8_9.features.world.redstone_helper.RenderObject {

		public final int power;

		public Wire(BlockPos pos, int power) {
			super(TextureType.ROTATING_TEXT, pos);
			this.power = power;
		}

		public double getYOffset() { return 0.02f; }
		public double[] getTexData() { return VertexDataStorage.getTexData(power); }
		public double[] getOffsets(int rotation) { return VertexDataStorage.getVertexOffsets(power)[rotation]; }

		@Override
		public boolean shouldRender() {
			if (!FileProvider.getSingleton(RedstoneHelper.class).isEnabled() || !showPower.get())
				return false;

			return power > 0 || showZeroPower.get();
		}

		@Override
		public boolean equals(dev.l3g7.griefer_utils.v1_8_9.features.world.redstone_helper.RenderObject r) {
			return super.equals(r) && power == ((Wire) r).power;
		}

	}

	public static class Hopper extends dev.l3g7.griefer_utils.v1_8_9.features.world.redstone_helper.RenderObject {

		private final int facing;

		public Hopper(BlockPos pos, EnumFacing facing) {
			super(TextureType.ARROWS, pos);
			this.facing = facing.getHorizontalIndex();
		}

		@Override
		public double getYOffset() {
			return 0.7f;
		}

		@Override
		public double[] getTexData() {
			return VertexDataStorage.getTexData("⬆");
		}

		@Override
		public double[] getOffsets(int rotation) {
			return VertexDataStorage.getVertexOffsets("⬆")[facing];
		}

		@Override
		public boolean shouldRender() {
			if (!FileProvider.getSingleton(RedstoneHelper.class).isEnabled())
				return false;

			return showDirection.get();
		}

		@Override
		public boolean equals(dev.l3g7.griefer_utils.v1_8_9.features.world.redstone_helper.RenderObject r) {
			return super.equals(r) && facing == ((Hopper) r).facing;
		}

	}

	private class Dropper extends RenderObject {

		private final EnumFacing dir;

		private Dropper(EnumFacing dir) {
			super(RedstoneHelper.this);
			this.dir = dir;
		}

		public void render(BlockPos pos, float partialTicks, int chunksFromPlayer) {
			if (!showDirection.get() || (range.get() != -1 && range.get() < chunksFromPlayer))
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

	private class TextRRO extends RenderObject {

		private final SwitchSetting setting;
		private final Supplier<Object> textSupplier;

		private TextRRO(SwitchSetting setting, Supplier<Object> textSupplier) {
			super(RedstoneHelper.this);
			this.textSupplier = textSupplier;
			this.setting = setting;
		}

		@Override
		public void render(BlockPos pos, float partialTicks, int chunksFromPlayer) {
			if (!setting.get() || (range.get() != -1 && range.get() < chunksFromPlayer))
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
