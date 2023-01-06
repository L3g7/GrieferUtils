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

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.render.ParticleSpawnEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.util.misc.Vec3d;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.labymod.utils.Material.COMPASS;
import static net.labymod.utils.Material.REDSTONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

/**
 * Implements cursor movement, selection and copy and paste in the sign edit gui.
 */
@Singleton
public class RedstoneHelper extends Feature {

	private static final int REDSTONE_PARTICLE_ID = EnumParticleTypes.REDSTONE.getParticleID();

	private final BooleanSetting showZeroPower = new BooleanSetting()
		.name("0 anzeigen")
		.icon(REDSTONE);

	private final BooleanSetting showPower = new BooleanSetting()
		.name("Redstone-Stärke anzeigen")
		.icon(REDSTONE)
		.subSettings(showZeroPower);

	private final BooleanSetting showDirection = new BooleanSetting()
		.name("Richtung anzeigen")
		.description("Zeigt die Richtung von Werfern / Spendern und Trichtern.")
		.icon(COMPASS);

	private final BooleanSetting hideRedstoneParticles = new BooleanSetting()
		.name("Redstone-Partikel verstecken")
		.icon(REDSTONE)
		.defaultValue(true);

	@MainElement
	private final CategorySetting enabled = new CategorySetting()
		.name("Redstone")
		.icon(REDSTONE)
		.subSettings(showPower, showDirection, hideRedstoneParticles);

	// hide Redstone dust
	// Repeater delay

	@EventListener
	public void onParticleSpawn(ParticleSpawnEvent event) {
		if (event.particleID == REDSTONE_PARTICLE_ID && hideRedstoneParticles.get())
			event.setCanceled(true);
	}

	@EventListener
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		if (!showPower.get() && !showDirection.get())
			return;

		GlStateManager.disableDepth();
		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();
		EntityPlayer p = Minecraft.getMinecraft().thePlayer;
		int px = (int) p.posX;
		int py = (int) p.posY;
		int pz = (int) p.posZ;

		int chunkXEnd = (px >> 4) + 2;
		int chunkZEnd = (pz >> 4) + 2;
		for (int cx = (px >> 4) - 2; cx < chunkXEnd; cx++) {
			for (int cz = (pz >> 4) - 2; cz < chunkZEnd; cz++) {
				Chunk chunk = Minecraft.getMinecraft().theWorld.getChunkProvider().provideChunk(cx, cz);
				int chunkYEnd = (py >>> 4) + 2;
				for (int cy = (py >>> 4) - 2; cy < chunkYEnd; cy++) {
					if (cy < 0) cy = 0;
					ExtendedBlockStorage ebs = chunk.getBlockStorageArray()[cy];
					if (ebs == null)
						continue;

					for (int x = 0; x < 16; x++)
						for (int y = 0; y < 16; y++)
							for (int z = 0; z < 16; z++)
								processBlock(cx << 4 | x, cy << 4 | y, cz << 4 | z, event.partialTicks, ebs.get(x, y, z));
				}
			}
		}

		GlStateManager.disableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.enableCull();
	}

	private void processBlock(int x, int y, int z, float partialTicks, IBlockState state) {
		Block block = state.getBlock();

		// Render power
		if (showPower.get()) {
			if (block == Blocks.redstone_wire) {
				int power = state.getValue(BlockRedstoneWire.POWER);
				if (power > 0 || showZeroPower.get()) {
					FontRenderer font = mc().fontRendererObj;

					prepareRender(new Vec3d(x, y + 0.02, z), partialTicks);
					String str = String.valueOf(power);

					GlStateManager.scale(0.035, 0.035, 0.035);
					GlStateManager.rotate(90, 1, 0, 0);
					GlStateManager.rotate(180 + mc().getRenderManager().playerViewY, 0, 0, 1);
					GlStateManager.translate(-(font.getStringWidth(str) - 1) / 2d, -(font.FONT_HEIGHT / 2d - 1), 0);

					font.drawString(str, 0, 0, 0xFFFFFF);

					GlStateManager.popMatrix();
				}
			}
		}

		// Render direction
		if (showDirection.get()) {
			boolean isHopper = block == Blocks.hopper;
			if (isHopper || block == Blocks.dropper || block == Blocks.dispenser) {
				EnumFacing dirVec = state.getValue(BlockDispenser.FACING);

				if (isHopper && dirVec.getAxis() == EnumFacing.Axis.Y)
					return;

				Vec3d loc = new Vec3d(x, y, z);

				prepareRender(loc, partialTicks);
				switch (dirVec) {
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
					Minecraft.getMinecraft().fontRendererObj.drawString("⬅", 0, 0, 0xFFFFFF);
				} else {
					Minecraft.getMinecraft().fontRendererObj.drawString("⬅", 0, 0, 0);

					GlStateManager.translate(0, 0, 10.2);

					Minecraft.getMinecraft().fontRendererObj.drawString("⬅", 0, 0, 0);
					GlStateManager.translate(0, 10.2, -10.35);

					GlStateManager.rotate(90, 1, 0, 0);
					Minecraft.getMinecraft().fontRendererObj.drawString("⬅", 0, 0, 0);

					GlStateManager.translate(0, 0, 10.5);
					Minecraft.getMinecraft().fontRendererObj.drawString("⬅", 0, 0, 0);
				}

				GlStateManager.popMatrix();
			}
		}
	}

	private void prepareRender(Vec3d loc, float partialTicks) {
		GlStateManager.pushMatrix();
		Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
		double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
		double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
		double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;
		double tx = (int) loc.x - viewerX + 0.5;
		double ty = loc.y - viewerY;
		double tz = (int) loc.z - viewerZ + 0.5;
		GlStateManager.translate(tx, ty, tz);

		GlStateManager.enableDepth();
		GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.enableTexture2D();
	}

}
