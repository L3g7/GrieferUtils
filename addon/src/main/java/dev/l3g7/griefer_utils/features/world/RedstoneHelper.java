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

package dev.l3g7.griefer_utils.features.world;

import com.google.common.base.Strings;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.event.NoteBlockPlayEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.WorldBlockOverlayRenderer;
import dev.l3g7.griefer_utils.misc.WorldBlockOverlayRenderer.RenderObject;
import dev.l3g7.griefer_utils.misc.WorldBlockOverlayRenderer.RenderObjectGenerator;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
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

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.labymod.utils.Material.*;
import static net.minecraft.init.Blocks.*;

/**
 * Implements cursor movement, selection and copy and paste in the sign edit gui.
 */
@Singleton
public class RedstoneHelper extends Feature implements RenderObjectGenerator {

	private static final BooleanSetting showZeroPower = new BooleanSetting()
		.name("0 anzeigen")
		.icon(REDSTONE);

	private static final BooleanSetting showPower = new BooleanSetting()
		.name("Redstone-Stärke anzeigen")
		.icon(REDSTONE)
		.subSettings(showZeroPower);

	private static final BooleanSetting showDirection = new BooleanSetting()
		.name("Richtung anzeigen")
		.description("Zeigt die Richtung von Werfern / Spendern und Trichtern.")
		.icon(COMPASS);

	private static final BooleanSetting showNoteId = new BooleanSetting()
		.name("Ton-ID anzeigen")
		.description("Ob der Name des Tons oder die ID angezeigt werden soll.")
		.icon(NOTE_BLOCK)
		.defaultValue(true);

	private static final BooleanSetting showNoteBlockPitch = new BooleanSetting()
		.name("Notenblock-Höhe anzeigen")
		.description("Zeigt an, welche Tonhöhe bei Notenblöcken eingestellt ist."
			+ "\nDafür muss von diesem Block ein Ton abgespielt worden sein.")
		.icon(NOTE_BLOCK)
		.subSettings(showNoteId);

	private static final BooleanSetting showCauldronLevel = new BooleanSetting()
		.name("Kessel-Füllstand anzeigen")
		.icon(CAULDRON_ITEM);

	private static final NumberSetting range = new NumberSetting()
		.name("Radius")
		.description("Der Radius um den Spieler in Chunks, in dem die Informationen angezeigt werden."
			+ "\n(-1 ist unendlich)"
			+ "\n(Betrifft nicht Schematics)")
		.defaultValue(-1)
		.min(-1)
		.icon(COMPASS);

	public static final BooleanSetting hideRedstoneParticles = new BooleanSetting()
		.name("Redstone-Partikel verstecken")
		.icon(REDSTONE)
		.defaultValue(true);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Redstone-Helfer")
		.description("Hilft beim Arbeiten mit Redstone.")
		.icon(REDSTONE)
		.subSettings(showPower, showDirection, showNoteBlockPitch, showCauldronLevel, range, new HeaderSetting(), hideRedstoneParticles);

	@Override
	public void init() {
		super.init();
		WorldBlockOverlayRenderer.registerRenderObjectGenerator(this);
	}

	@Override
	public int getRange() {
		return range.get();
	}

	@Override
	public RenderObject getRenderObject(IBlockState state, BlockPos pos, WorldClient world) {
		Block block = state.getBlock();
		if (block == redstone_wire)
			return new Wire(state.getValue(BlockRedstoneWire.POWER));

		if (block == cauldron)
			return new TextRRO(showCauldronLevel, () -> state.getValue(BlockCauldron.LEVEL));

		boolean isHopper = block == hopper;
		if (!isHopper && block != dropper && block != dispenser)
			return null;

		EnumFacing dir = state.getValue(BlockDispenser.FACING);
		if (isHopper && dir == EnumFacing.DOWN)
			return null;

		return new Directional(dir, isHopper);
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

	private class Wire extends RenderObject {
		private final int power;

		private Wire(int power) {
			super(RedstoneHelper.this);
			this.power = power;
		}

		public void render(BlockPos pos, float partialTicks) {
			if (!showPower.get())
				return;

			if (power <= 0 && !showZeroPower.get())
				return;

			FontRenderer font = mc().fontRendererObj;

			prepareRender(new Vec3d(pos.getX(), pos.getY() + 0.02, pos.getZ()), partialTicks);
			String str = String.valueOf(power);

			GlStateManager.scale(0.035, 0.035, 0.035);
			GlStateManager.rotate(90, 1, 0, 0);
			GlStateManager.rotate(180 + mc().getRenderManager().playerViewY, 0, 0, 1);
			GlStateManager.translate(-(font.getStringWidth(str) - 1) / 2d, -(font.FONT_HEIGHT / 2d - 1), 0);

			font.drawString(str, 0, 0, 0xFFFFFF);

			GlStateManager.popMatrix();
		}
	}

	private class Directional extends RenderObject {
		private final EnumFacing dir;
		private final boolean isHopper;

		private Directional(EnumFacing dir, boolean isHopper) {
			super(RedstoneHelper.this);
			this.dir = dir;
			this.isHopper = isHopper;
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

			if (isHopper) {
				GlStateManager.translate(0, 6.9, 0);
				GlStateManager.rotate(90, 1, 0, 0);
				mc().fontRendererObj.drawString("⬅", 0, 0, 0xFFFFFF);
			} else {
				mc().fontRendererObj.drawString("⬅", 0, 0, 0);

				GlStateManager.translate(0, 0, 10.2);

				mc().fontRendererObj.drawString("⬅", 0, 0, 0);
				GlStateManager.translate(0, 10.2, -10.35);

				GlStateManager.rotate(90, 1, 0, 0);
				mc().fontRendererObj.drawString("⬅", 0, 0, 0);

				GlStateManager.translate(0, 0, 10.5);
				mc().fontRendererObj.drawString("⬅", 0, 0, 0);
			}

			GlStateManager.popMatrix();
		}
	}

	private class TextRRO extends RenderObject {

		private final BooleanSetting setting;
		private final Supplier<Object> textSupplier;

		private TextRRO(BooleanSetting setting, Supplier<Object> textSupplier) {
			super(RedstoneHelper.this);
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
