/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building.redstone_helper;

import com.google.common.base.Strings;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.render.render_engine.ChunkCompiler;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderChunkManager;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderObject;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.Vertices;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Random;

import static net.minecraft.init.Blocks.*;

/**
 * Implements cursor movement, selection and copy and paste in the sign edit gui.
 */
@Singleton
public class RedstoneHelper extends Feature implements RenderObject.Generator {

	private static final TextureCutout ARROW_TEXTURE = new TextureCutout("griefer_utils", "textures/arrow.png");

	private static final SwitchSetting showZeroPower = SwitchSetting.create()
		.name("0 anzeigen")
		.description("Ob die Stärke-Anzeige auch angezeigt werden soll, wenn die Stärke 0 beträgt.")
		.icon("redstone_repeater")
		.callback(RenderChunkManager::reloadAll);

	private static final SwitchSetting showPower = SwitchSetting.create()
		.name("Redstone-Stärke anzeigen")
		.description("Zeigt auf Redstone-Kabeln ihre derzeitige Stärke an.")
		.icon("redstone")
		.subSettings(showZeroPower);

	private static final SwitchSetting showDirection = SwitchSetting.create()
		.name("Richtung anzeigen")
		.description("Zeigt die Richtung von Werfern / Spendern und Trichtern.")
		.icon("axes");

	private static final SwitchSetting showNoteId = SwitchSetting.create()
		.name("Ton-ID anzeigen")
		.description("Ob der Name des Tons oder die ID angezeigt werden soll.")
		.icon("sounds")
		.defaultValue(true);

	private static final SwitchSetting showNoteBlockPitch = SwitchSetting.create()
		.name("Notenblock-Höhe anzeigen")
		.description("Zeigt an, welche Tonhöhe bei Notenblöcken eingestellt ist.")
		.icon("sounds")
		.subSettings(showNoteId);

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
		.subSettings(showPower, showDirection, showNoteBlockPitch, showCauldronLevel, HeaderSetting.create(), hideRedstoneParticles);

	public static RedstoneHelper get() {
		return get(RedstoneHelper.class);
	}

	@Override
	public void init() {
		super.init();
		RenderObject.registerGenerator(this);
	}

	@Override
	public RenderObject getRenderObject(BlockPos worldPos, IBlockState state, WorldClient world) {
		Block block = state.getBlock();

		if (block == redstone_wire) {
			if (!showPower.get())
				return null;

			int power = state.getValue(BlockRedstoneWire.POWER);
			if (power == 0 && !showZeroPower.get())
				return null;

			return new Wire(state.getValue(BlockRedstoneWire.POWER));

		}

		if (block == hopper) {
			if (!showDirection.get())
				return null;
			EnumFacing facing = state.getValue(BlockHopper.FACING);
			return facing.getHorizontalIndex() == -1 ? null : new Hopper(facing);
		}

		if (block == dropper || block == dispenser) {
			if (!showDirection.get())
				return null;
			return new Dropper(state.getValue(BlockDispenser.FACING));
		}

		if (block == noteblock) {
			if (!showNoteBlockPitch.get())
				return null;

			if (!(world.getTileEntity(worldPos) instanceof TileEntityNote noteBlock))
				return null;

			String[] NOTES = new String[] { "Fis", "G", "Gis", "A", "Ais", "B", "C", "Cis", "D", "Dis", "E", "F" };
			String note = NOTES[noteBlock.note % 12];
			int octave = (noteBlock.note / 12) + 1;

			return new TextRenderObject(note + Strings.repeat("'", octave));
		}

		if (block == cauldron) {
			if (!showCauldronLevel.get())
				return null;

			return new TextRenderObject(String.valueOf(state.getValue(BlockCauldron.LEVEL)));
		}

		return null;
	}

	@Mixin(BlockRedstoneWire.class)
	private static class MixinBlockRedstoneWire {

		@Inject(method = "randomDisplayTick", at = @At("HEAD"), cancellable = true)
		private void injectRandomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
			if (RedstoneHelper.get().isEnabled() && hideRedstoneParticles.get())
				ci.cancel();
		}

	}

	public static class Wire extends RenderObject {

		private final int power;

		public Wire(int power) {
			this.power = power;
		}

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof Wire w && power == w.power;
		}

		@Override
		public void draw(ChunkCompiler compiler, int rotation) {
			for (FontRenderData.CharRenderInfo renderInfo : FontRenderData.getCharRenderInfos(String.valueOf(power))) {
				Vertices offsets = renderInfo.rotatedVertices()[rotation];
				compiler.render(renderInfo.texture(), offsets.translate(0, -0.48, 0));
			}
		}
	}

	private static class Hopper extends RenderObject {

		private static final Vertices BASE_VERTICES = Vertices.onYPlane(0.7, 0.7);

		private final EnumFacing dir;
		private final Vertices vertices;

		private Hopper(EnumFacing dir) {
			this.dir = dir;
			this.vertices = BASE_VERTICES
				.rotateY(-90 * dir.getHorizontalIndex())
				.translate(0, 0.201, 0);
		}

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof Hopper h && dir == h.dir;
		}

		@Override
		public void draw(ChunkCompiler compiler, int rotation) {
			compiler.render(ARROW_TEXTURE, vertices);
		}

	}

	private static class Dropper extends RenderObject {
		private static final Vertices BASE_VERTICES = Vertices.onYPlane(1, 1)
			.translate(0, 0.51, 0);

		private final EnumFacing dir;

		private Dropper(EnumFacing dir) {
			this.dir = dir;
		}

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof Dropper d && dir == d.dir;
		}

		@Override
		public void draw(ChunkCompiler compiler, int rotation) {
			compiler.setColor(Color.BLACK);
			compiler.setTransformation(v -> {
				if (dir == EnumFacing.UP)
					return v.rotateX(270);
				if (dir == EnumFacing.DOWN)
					return v.rotateX(90);
				return v.rotateY(-90 * dir.getHorizontalIndex());
			});

			compiler.render(ARROW_TEXTURE,
				BASE_VERTICES,
				BASE_VERTICES.rotateZ(90),
				BASE_VERTICES.rotateZ(180),
				BASE_VERTICES.rotateZ(270)
			);
		}
	}

	private static class TextRenderObject extends RenderObject {

		private final String text;

		private TextRenderObject(String text) {
			this.text = text;
		}

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof TextRenderObject t && text.equals(t.text);
		}

		@Override
		public void draw(ChunkCompiler compiler, int rotation) {
			for (FontRenderData.CharRenderInfo renderInfo : FontRenderData.getCharRenderInfos(text)) {
				Vertices offsets = renderInfo.rotatedVertices()[0];

				Vertices base = offsets.translate(0, 0.51, 0);
				Vertices sideBase = offsets.translate(0, 0.51, 0).rotateX(270);

				compiler.render(renderInfo.texture(),
					base, // up
					base.rotateX(180).translate(0, 0.18, 0), // down
					sideBase,
					sideBase.rotateY(90),
					sideBase.rotateY(180),
					sideBase.rotateY(270)
				);
			}
		}
	}

}
