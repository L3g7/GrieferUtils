/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.NoteBlockPlayEvent;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.render.render_engine.ChunkCompiler;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderObject;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.Quad;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.text.TextGeometry;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.text.TextGeometry.GlyphModel;
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

import java.awt.*;
import java.util.Random;

import static net.minecraft.init.Blocks.*;

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
		.subSettings(showZeroPower)
		.callback(RenderChunkManager::reloadAll);

	private static final SwitchSetting showDirection = SwitchSetting.create()
		.name("Richtung anzeigen")
		.description("Zeigt die Richtung von Werfern / Spendern und Trichtern.")
		.icon("axes")
		.callback(RenderChunkManager::reloadAll);

	private static final SwitchSetting showNoteId = SwitchSetting.create()
		.name("Ton-ID anzeigen")
		.description("Ob die ID des Tons anstatt des Namens angezeigt werden soll.")
		.icon("sounds")
		.callback(RenderChunkManager::reloadAll);

	private static final SwitchSetting showNoteBlockPitch = SwitchSetting.create()
		.name("Notenblock-Höhe anzeigen")
		.description("Zeigt an, welche Tonhöhe bei Notenblöcken eingestellt ist.")
		.icon("sounds")
		.subSettings(showNoteId)
		.callback(RenderChunkManager::reloadAll);

	private static final SwitchSetting showCauldronLevel = SwitchSetting.create()
		.name("Kessel-Füllstand anzeigen")
		.description("Zeigt an einem Kessel seinen derzeitigen Füllstand an.")
		.icon("water_breathing")
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

			EnumFacing facing = state.getValue(BlockHopper.FACING);
			return facing.getHorizontalIndex() == -1 ? null : new Hopper(facing);
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

			return new TextRenderObject(String.valueOf(state.getValue(BlockCauldron.LEVEL)), 0.18);
		}

		// Nothing
		return null;
	}

	@EventListener
	private static void onNoteBlockPlay(NoteBlockPlayEvent event) {
		if (!showNoteBlockPitch.get())
			return;

		String text = showNoteId.get() ? String.valueOf(event.noteId) : event.note;

		WorldRenderChunkManager manager = WorldRenderChunkManager.get();
		manager.setRenderObject(event.pos, new TextRenderObject(text, 0));
	}

	@Mixin(BlockRedstoneWire.class)
	private static class MixinBlockRedstoneWire {

		@Inject(method = "randomDisplayTick", at = @At("HEAD"), cancellable = true)
		private void injectRandomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
			if (RedstoneHelper.get().isEnabled() && hideRedstoneParticles.get())
				ci.cancel();
		}

	}

	private record Wire(int power) implements RenderObject {

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof Wire(int other) && power == other;
		}

		@Override
		public boolean rotates() {
			return true;
		}

		@Override
		public void draw(ChunkCompiler compiler, int rotation) {
			for (GlyphModel renderInfo : TextGeometry.getGlyphModels(String.valueOf(power))) {
				Quad offsets = renderInfo.rotatedVertices()[rotation];
				compiler.draw(renderInfo.texture(), offsets.translate(0, -0.48, 0));
			}
		}
	}

	private record Hopper(EnumFacing dir, Quad quad) implements RenderObject {

		private static final Quad BASE_QUAD = Quad.onYPlane(0.7, 0.7);

		private Hopper(EnumFacing dir) {
			this(dir, BASE_QUAD
				.rotateY(-90 * dir.getHorizontalIndex())
				.translate(0, 0.201, 0));
		}

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof Hopper h && dir == h.dir;
		}

		@Override
		public void draw(ChunkCompiler compiler, int rotation) {
			compiler.draw(ARROW_TEXTURE, quad);
		}
	}

	private record Dropper(EnumFacing dir) implements RenderObject {

		private static final Quad BASE_QUAD = Quad.onYPlane(1, 1)
			.translate(0, 0.51, 0);

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof Dropper(EnumFacing other) && dir == other;
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

			compiler.draw(ARROW_TEXTURE,
				BASE_QUAD,
				BASE_QUAD.rotateZ(90),
				BASE_QUAD.rotateZ(180),
				BASE_QUAD.rotateZ(270)
			);
		}
	}

	private record TextRenderObject(String text, double bottomYDelta) implements RenderObject {

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof TextRenderObject(String otherText, double otherY)
				&& text.equals(otherText) && bottomYDelta == otherY;
		}

		@Override
		public void draw(ChunkCompiler compiler, int rotation) {
			for (GlyphModel renderInfo : TextGeometry.getGlyphModels(text)) {
				Quad offsets = renderInfo.rotatedVertices()[0];

				Quad base = offsets.translate(0, 0.51, 0);
				Quad sideBase = offsets.translate(0, 0.51, 0).rotateX(270);

				compiler.draw(renderInfo.texture(),
					base,
					base.rotateX(180).translate(0, bottomYDelta, 0),
					sideBase,
					sideBase.rotateY(90),
					sideBase.rotateY(180),
					sideBase.rotateY(270)
				);
			}
		}
	}

}
