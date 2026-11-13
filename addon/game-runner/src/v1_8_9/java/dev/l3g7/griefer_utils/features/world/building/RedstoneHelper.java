/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.events.NoteBlockPlayEvent;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.render.render_engine.ChunkCompiler;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderChunk;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderObject;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.Quad;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import dev.l3g7.griefer_utils.core.util.render.render_engine.manager.RenderChunkManager;
import dev.l3g7.griefer_utils.core.util.render.render_engine.manager.WorldRenderChunkManager;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static net.minecraft.init.Blocks.*;

@Singleton
public class RedstoneHelper extends Feature implements RenderObject.Generator {

	private static final SwitchSetting showZeroPower = SwitchSetting.create()
		.name("0 anzeigen")
		.description("Ob die Stärke-Anzeige auch angezeigt werden soll, wenn die Stärke 0 beträgt.")
		.icon("redstone_repeater")
		.callback(RenderChunkManager::reloadAll);

	private static final SwitchSetting showPower = SwitchSetting.create()
		.name("Redstone-Stärke anzeigen")
		.description("Zeigt auf Redstone-Kabeln ihre derzeitige Stärke an.")
		.icon("redstone")
		.subSettings(showZeroPower)
		.callback(RenderChunkManager::reloadAll);

	public static final DropDownSetting<DisplayType> dropperDisplayType = DropDownSetting.create(DisplayType.class)
		.name("Dropper-Anzeigetyp")
		.icon("dropper")
		.defaultValue(DisplayType.DISTINCT)
		.callback(RenderChunkManager::reloadAll);

	public static final DropDownSetting<DisplayType> hopperDisplayType = DropDownSetting.create(DisplayType.class)
		.name("Trichter-Anzeigetyp")
		.icon("hopper")
		.defaultValue(DisplayType.DISTINCT)
		.callback(RenderChunkManager::reloadAll);

	public static final SwitchSetting showHopperSide = SwitchSetting.create()
		.name("Trichter-Richtung seitlich anzeigen")
		.icon("hopper")
		.defaultValue(true)
		.callback(RenderChunkManager::reloadAll);

	private static final SwitchSetting showDirection = SwitchSetting.create()
		.name("Richtung anzeigen")
		.description("Zeigt die Richtung von Werfern / Spendern und Trichtern.")
		.icon("axes")
		.subSettings(dropperDisplayType, hopperDisplayType, showHopperSide)
		.callback(RenderChunkManager::reloadAll);

	public static final SwitchSetting showNoteBlockInstrument = SwitchSetting.create()
		.name("Instrument anzeigen")
		.description("Ob das Instrument des Notenblocks angezeigt werden soll.")
		.icon("sounds")
		.defaultValue(true)
		.callback(RenderChunkManager::reloadAll);

	private static final SwitchSetting showNoteBlockPitch = SwitchSetting.create()
		.name("Notenblock-Höhe anzeigen")
		.description("Zeigt an, welche Tonhöhe bei Notenblöcken eingestellt ist.")
		.icon("sounds")
		.subSettings(showNoteBlockInstrument)
		.callback(RenderChunkManager::reloadAll);

	public static final DropDownSetting<DisplayType> cauldronDisplayType = DropDownSetting.create(DisplayType.class)
		.name("Anzeigetyp")
		.icon("lens")
		.defaultValue(DisplayType.DISTINCT)
		.callback(RenderChunkManager::reloadAll);

	public static final SwitchSetting showCauldronLevel = SwitchSetting.create()
		.name("Kessel-Füllstand anzeigen")
		.description("Zeigt an einem Kessel seinen derzeitigen Füllstand an.")
		.icon("water_breathing")
		.subSettings(cauldronDisplayType)
		.callback(RenderChunkManager::reloadAll);

	public static final SwitchSetting hideRedstoneParticles = SwitchSetting.create()
		.name("Redstone-Partikel verstecken")
		.description("Versteckt die Partikel, die durch aktivertes Redstone erzeugt werden.")
		.icon("redstone_particles")
		.defaultValue(true)
		.callback(RenderChunkManager::reloadAll);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Redstone-Helfer")
		.description("Hilft beim Arbeiten mit Redstone.")
		.icon("redstone_comparator")
		.subSettings(showPower, showDirection, showNoteBlockPitch, showCauldronLevel, HeaderSetting.create(), hideRedstoneParticles)
		.callback(RenderChunkManager::reloadAll);

	public static RedstoneHelper get() {
		return get(RedstoneHelper.class);
	}

	@Override
	public RenderObject getRenderObject(BlockPos worldPos, IBlockState state, WorldClient world) {
		Block block = state.getBlock();

		// Wire
		if (block == redstone_wire) {
			if (!showPower.get())
				return null;

			int power = state.getValue(BlockRedstoneWire.POWER);
			if (power == 0 && !showZeroPower.get())
				return null;

			return new Wire(state.getValue(BlockRedstoneWire.POWER));

		}

		// Hopper
		if (block == hopper) {
			if (!showDirection.get())
				return null;

			return new Hopper(state.getValue(BlockHopper.FACING));
		}

		// Dropper / Dispenser
		if (block == dropper || block == dispenser) {
			if (!showDirection.get())
				return null;

			return new Dropper(state.getValue(BlockDispenser.FACING));
		}

		// Cauldron
		if (block == cauldron) {
			if (!showCauldronLevel.get())
				return null;

			return new Cauldron(state.getValue(BlockCauldron.LEVEL));
		}

		// Nothing
		return null;
	}

	@EventListener
	private static void onNoteBlockPlay(NoteBlockPlayEvent event) {
		if (!showNoteBlockPitch.get())
			return;

		WorldRenderChunkManager manager = WorldRenderChunkManager.get();
		manager.setRenderObject(event.pos, new NoteBlock(event.instrument, event.noteId));
	}

	@Mixin(BlockRedstoneWire.class)
	private static class MixinBlockRedstoneWire {

		@Inject(method = "randomDisplayTick", at = @At("HEAD"), cancellable = true)
		private void injectRandomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
			if (RedstoneHelper.get().isEnabled() && hideRedstoneParticles.get())
				ci.cancel();
		}

	}

	public enum DisplayType implements Named {

		DISTINCT("Deutlich"), SUBTLE("Subtil");

		final String name;
		DisplayType(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

	private static TextureCutout tex(DropDownSetting<DisplayType> displayTypeSetting, String type, String name) {
		StringBuilder path = new StringBuilder("textures/redstone_helper_overlays/");
		path.append(type).append('/');

		if (displayTypeSetting != null) {
			boolean subtle = cauldronDisplayType.get() == DisplayType.SUBTLE;
			path.append(subtle ? "subtle" : "distinct").append('_');
		}

		path.append(name);
		path.append(".png");

		return new TextureCutout("griefer_utils", path.toString());
	}

	record Wire(int power, TextureCutout texture) implements RenderObject {

		private static final Quad BASE_QUAD = Quad.onYPlane(11 / 32f, 8 / 32f)
			.translate(0, -0.48, 0);

		public Wire(int power) {
			this(power, new TextureCutout("griefer_utils", "textures/numbers/" + power + ".png"));
		}

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof Wire w && power == w.power;
		}

		@Override
		public boolean rotates() {
			return true;
		}

		@Override
		public void draw(ChunkCompiler compiler, BlockPos chunkPosition, int rotation) {
			compiler.draw(texture, BASE_QUAD.rotateY(RenderChunk.ROTATION_SLICE * rotation));
		}

	}

	record Hopper(EnumFacing dir) implements RenderObject {

		private static TextureCutout tex(String name) {
			return RedstoneHelper.tex(hopperDisplayType, "hopper", name);
		}

		private static final Quad INNER_QUAD = Quad.onYPlane(1, 1).translate(0, 0.201, 0);
		private static final Quad BASE_SIDE_QUAD = Quad.onXPlane(1, 1).translate(0.501, 0, 0);

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof Hopper(EnumFacing otherDir) && dir == otherDir;
		}

		@Override
		public void draw(ChunkCompiler compiler, BlockPos chunkPosition, int rotation) {
			if (dir == EnumFacing.DOWN) {
				compiler.draw(tex("inner_down"), INNER_QUAD);
				if (!showHopperSide.get())
					return;

				compiler.draw(tex("side_down"),
					BASE_SIDE_QUAD.rotateY(90),
					BASE_SIDE_QUAD.rotateY(270)
				);

				compiler.setBrightness(0xCE);
				compiler.draw(tex("side_down"),
					BASE_SIDE_QUAD,
					BASE_SIDE_QUAD.rotateY(180)
				);
				return;
			}

			compiler.setTransformation(q -> q.rotateY(-90 * dir.getHorizontalIndex()));

			compiler.draw(tex("inner_lateral"), INNER_QUAD);
			if (!showHopperSide.get())
				return;

			compiler.setBrightness(dir.getAxis() == EnumFacing.Axis.X ? 0xFF : 0xCE);
			compiler.draw(tex("side_lateral"), BASE_SIDE_QUAD.translate(-1.002, 0, 0));
			compiler.draw(tex("side_lateral"), BASE_SIDE_QUAD);

			compiler.setBrightness(dir.getAxis() == EnumFacing.Axis.X ? 0xCE : 0xFF);
			compiler.draw(tex("side_back"), BASE_SIDE_QUAD.rotateY(90));
			compiler.draw(tex("side_front"), BASE_SIDE_QUAD.rotateY(-90));
		}
	}

	record Dropper(EnumFacing dir) implements RenderObject {

		private static TextureCutout tex(String name) {
			return RedstoneHelper.tex(dropperDisplayType, "dropper", name);
		}

		private static final Quad BASE_Y_QUAD = Quad.onYPlane(1, 1)
			.translate(0, 0.501, 0);

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof Dropper(EnumFacing other) && dir == other;
		}

		@Override
		public void draw(ChunkCompiler compiler, BlockPos chunkPosition, int rotation) {
			if (dir.getAxis() == EnumFacing.Axis.Y) {
				/*
				 * Vertical dropper
				 */
				if (dir == EnumFacing.UP)
					compiler.setTransformation(q -> q.rotateX(180));

				Quad side = BASE_Y_QUAD.rotateZ(90);

				compiler.draw(tex("lateral_base"),
					side.rotateY(90),
					side.rotateY(270)
				);

				if (dir == EnumFacing.UP)
					compiler.setBrightness(0x80);
				compiler.draw(tex("vertical_back"), BASE_Y_QUAD);

				compiler.setBrightness(0xCE);
				compiler.draw(tex("lateral_base"),
					side,
					side.rotateY(180)
				);
				return;
			}

			/*
			 * Horizontal dropper
			 */
			compiler.setTransformation(q -> q.rotateY(-90 * dir.getHorizontalIndex()));

			// Top, Bottom

			Quad baseYQuad = Quad.onYPlane(1, 1).rotateY(90);
			compiler.draw(tex("lateral_base"), baseYQuad.translate(0, 0.501, 0));
			compiler.setBrightness(0x80);
			compiler.draw(tex("lateral_base"), baseYQuad.translate(0, -0.501, 0));

			// Sides
			Quad baseXQuad = Quad.onXPlane(1, 1).translate(0.501, 0, 0);
			compiler.setBrightness(dir.getAxis() == EnumFacing.Axis.X ? 0xFF : 0xCE);
			compiler.draw(tex(dir.getAxis() == EnumFacing.Axis.Y ? "lateral_base" : "lateral_side"),
				baseXQuad,
				baseXQuad.translate(-1.002, 0, 0)
			);

			// Back
			compiler.setBrightness(dir.getAxis() == EnumFacing.Axis.X ? 0xCE : 0xFF);
			compiler.draw(tex(dir.getAxis() == EnumFacing.Axis.Y ? "vertical_back" : "horizontal_back"),
				baseXQuad.rotateY(90)
			);
		}

	}

	record NoteBlock(int instrument, int noteId) implements RenderObject {

		private static TextureCutout tex(String name) {
			return RedstoneHelper.tex(null, "note_block", name);
		}

		private static final Quad BASE_QUAD = Quad.onXPlane(1, 1)
			.translate(-0.501, 0, 0);

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof NoteBlock n && instrument == n.instrument && noteId == n.noteId;
		}

		@Override
		public void draw(ChunkCompiler compiler, BlockPos chunkPosition, int rotation) {
			if (noteId >= 0 && noteId <= 24)
				drawTexture(compiler, tex("notes/" + noteId));
			if (showNoteBlockInstrument.get() && instrument >= 1 && instrument <= 4)
				drawTexture(compiler, tex("instruments/" + instrument));
		}

		private void drawTexture(ChunkCompiler compiler, TextureCutout texture) {
			compiler.draw(texture, // Light
				BASE_QUAD.rotateY(90), // South
				BASE_QUAD.rotateY(270), // North
				BASE_QUAD.rotateZ(270) // Up
			);

			compiler.setBrightness(0xCE);

			compiler.draw(texture, // Dark
				BASE_QUAD, // West
				BASE_QUAD.rotateY(180) // East
			);

			compiler.setBrightness(0x80);
			compiler.draw(texture, BASE_QUAD.rotateZ(90)); // Down
		}
	}

	record Cauldron(int level, TextureCutout texture) implements RenderObject {

		private static final Quad BASE_QUAD = Quad.onXPlane(1, 1)
			.translate(-0.501, 0, 0);

		public Cauldron(int level) {
			this(level, tex(cauldronDisplayType, "cauldron", "" + level));
		}

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof Cauldron c && level == c.level;
		}

		@Override
		public void draw(ChunkCompiler compiler, BlockPos chunkPosition, int rotation) {
			compiler.draw(texture, // Light
				BASE_QUAD.rotateY(90),
				BASE_QUAD.rotateY(270)
			);

			if (cauldronDisplayType.get() == DisplayType.SUBTLE)
				compiler.setBrightness(0xC0);

			compiler.draw(texture, // Dark
				BASE_QUAD,
				BASE_QUAD.rotateY(180)
			);

			if (cauldronDisplayType.get() == DisplayType.DISTINCT)
				compiler.setBrightness(0xC0);
			compiler.draw(texture, // Bottom
				BASE_QUAD.rotateZ(90).translate(1 / 16f, 0.18f, 0)
			);
		}
	}
}
