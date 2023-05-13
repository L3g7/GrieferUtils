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

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.ChunkFilledEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import io.netty.util.internal.ConcurrentSet;
import net.labymod.utils.Material;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static org.lwjgl.opengl.GL11.*;

@Singleton
public class LightLevelOverlay extends Feature {

	private final Map<ChunkCoordIntPair, Map<BlockPos, Integer>> lightPositions = new ConcurrentHashMap<>();
	private final Set<ChunkCoordIntPair> queuedChunkChecks = new ConcurrentSet<>();
	private int currentSkyLight = -1;

	private final DropDownSetting<RenderMode> renderMode = new DropDownSetting<>(RenderMode.class)
		.name("Anzeigemodus")
		.description("Wie das Lightlevel angezeigt werden soll.")
		.icon("lens")
		.defaultValue(RenderMode.NUMBERS);

	private final NumberSetting range = new NumberSetting()
		.name("Radius (Chunks)")
		.description("Der Radius um den Spieler in Chunks, in dem das Lichtlevel angezeigt wird.")
		.defaultValue(1)
		.icon(Material.COMPASS);

	private final NumberSetting updateDelay = new NumberSetting()
		.name("Update-Wartezeit (Ticks)")
		.description("Wie lange nach einen Blockupdate gewartet werden soll, um die Lichtlevel neu zu berechnen."
			+ "\nHöhere Werte sind empfohlen, wenn viele Blöcke platzieren / abgebaut werden.")
		.defaultValue(5)
		.icon(Material.WATCH);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Lichtlevel anzeigen")
		.description("Zeigt das Lichtlevel auf Blöcken an")
		.icon("light_bulb")
		.subSettings(range, updateDelay, renderMode)
		.callback(b -> {
			if (!b) {
				lightPositions.clear();
				return;
			}

			if (world() == null)
				return;

			List<Chunk> chunkListing = Reflection.get(world().getChunkProvider(), "chunkListing");
			for (Chunk chunk : chunkListing)
				enqueueChunkUpdate(chunk.getChunkCoordIntPair());
		});

	@EventListener
	private void onChunkFilledEvent(ChunkFilledEvent event) {
		updateChunk(event.getChunk());
	}

	@EventListener
	private void onChunkUnload(ChunkEvent.Unload event) {
		lightPositions.remove(event.getChunk().getChunkCoordIntPair());
	}

	@EventListener
	private void onServerChange(ServerEvent.ServerSwitchEvent event) {
		lightPositions.clear();
	}

	@EventListener
	private void onPacket(PacketEvent.PacketReceiveEvent event) {
		if (event.packet instanceof S23PacketBlockChange) {
			S23PacketBlockChange packet = (S23PacketBlockChange) event.packet;
			onBlockUpdate(packet.getBlockPosition());
		}

		if (event.packet instanceof S03PacketTimeUpdate) {
			TickScheduler.runAfterRenderTicks(() -> {
				if (world() == null)
					return;

				int skyLightSub = world().calculateSkylightSubtracted(1);
				if (currentSkyLight == -1) {
					currentSkyLight = skyLightSub;
				} else if (currentSkyLight != skyLightSub) {
					currentSkyLight = skyLightSub;
					List<Chunk> chunkListing = Reflection.get(world().getChunkProvider(), "chunkListing");
					for (Chunk chunk : chunkListing)
						enqueueChunkUpdate(chunk.getChunkCoordIntPair());
				}
			}, 1);
		}

		if (!(event.packet instanceof S22PacketMultiBlockChange))
			return;

		S22PacketMultiBlockChange packet = (S22PacketMultiBlockChange) event.packet;
		for (S22PacketMultiBlockChange.BlockUpdateData data : packet.getChangedBlocks())
			onBlockUpdate(data.getPos());
	}

	private void onBlockUpdate(BlockPos pos) {
		int x = pos.getX() >> 4;
		int z = pos.getZ() >> 4;

		enqueueChunkUpdate(new ChunkCoordIntPair(x + 1, z + 1));
		enqueueChunkUpdate(new ChunkCoordIntPair(x + 1, z));
		enqueueChunkUpdate(new ChunkCoordIntPair(x + 1, z - 1));
		enqueueChunkUpdate(new ChunkCoordIntPair(x, z + 1));
		enqueueChunkUpdate(new ChunkCoordIntPair(x, z));
		enqueueChunkUpdate(new ChunkCoordIntPair(x, z - 1));
		enqueueChunkUpdate(new ChunkCoordIntPair(x - 1, z + 1));
		enqueueChunkUpdate(new ChunkCoordIntPair(x - 1, z));
		enqueueChunkUpdate(new ChunkCoordIntPair(x - 1, z - 1));
	}

	private void enqueueChunkUpdate(ChunkCoordIntPair ccip) {
		if (queuedChunkChecks.isEmpty()) {
			TickScheduler.runAfterClientTicks(() -> {
				for (ChunkCoordIntPair pair : queuedChunkChecks)
					updateChunk(world().getChunkFromChunkCoords(pair.chunkXPos, pair.chunkZPos));

				queuedChunkChecks.clear();
			}, updateDelay.get());
		}

		queuedChunkChecks.add(ccip);
	}

	private void updateChunk(Chunk chunk) {
		if (world() == null || player() == null)
			return;

		Map<BlockPos, Integer> lights = new ConcurrentHashMap<>();

		int skyLightSub = world().calculateSkylightSubtracted(1);
		for (ExtendedBlockStorage ebs : chunk.getBlockStorageArray()) {
			if (ebs == null)
				continue;

			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						if (ebs.getBlockByExtId(x, y & 15, z) != Blocks.air)
							continue;

						BlockPos pos = new BlockPos(x + chunk.xPosition * 16, y + ebs.getYLocation(), z + chunk.zPosition * 16);
						if (!chunk.getBlock(pos.down()).isSideSolid(world(), pos.down(), EnumFacing.UP))
							continue;

						lights.put(pos, chunk.getLightSubtracted(pos, skyLightSub));
					}
				}
			}
		}

		if (lights.isEmpty())
			lightPositions.remove(chunk.getChunkCoordIntPair());
		else
			lightPositions.put(chunk.getChunkCoordIntPair(), lights);
	}

	@EventListener
	private void onRenderWorldLast(RenderWorldLastEvent event) {
		if (renderMode.get() == RenderMode.NUMBERS)
			renderNumber(event.partialTicks);
		else
			renderPlanes();
	}

	private void renderNumber(float partialTicks) {
		Entity viewer = mc().getRenderViewEntity();
		double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
		double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
		double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;

		for (Map.Entry<ChunkCoordIntPair, Map<BlockPos, Integer>> chunkMap : lightPositions.entrySet()) {
			ChunkCoordIntPair coords = chunkMap.getKey();
			if (Math.abs(coords.chunkXPos - player().chunkCoordX) > range.get()
				|| Math.abs(coords.chunkZPos - player().chunkCoordZ) > range.get())
				continue;

			for (Map.Entry<BlockPos, Integer> entry : chunkMap.getValue().entrySet()) {
				GlStateManager.pushMatrix();
				GlStateManager.enableDepth();
				GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
				GlStateManager.enableTexture2D();

				double tx = entry.getKey().getX() - viewerX + 0.5;
				double ty = entry.getKey().getY() - viewerY + 0.01;
				double tz = entry.getKey().getZ() - viewerZ + 0.5;
				GlStateManager.translate(tx, ty, tz);

				FontRenderer font = mc().fontRendererObj;

				String str = String.valueOf(entry.getValue());

				GlStateManager.scale(0.0625, 0.05, 0.0625);
				GlStateManager.rotate(90, 1, 0, 0);
				GlStateManager.rotate(180 + mc().getRenderManager().playerViewY, 0, 0, 1);
				GlStateManager.translate(-(font.getStringWidth(str) - 1) / 2d, -(font.FONT_HEIGHT / 2d - 1), 0);

				int color = (17 * entry.getValue()) << 8 | 0xFF0000;
				font.drawString(str, 0, 0, color);
				GlStateManager.popMatrix();
			}
		}

	}

	private void renderPlanes() {
		Entity entity = mc().getRenderViewEntity();
		Vec3d prevPos = new Vec3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
		Vec3d cam = prevPos.add(pos(entity).subtract(prevPos).scale(partialTicks()));

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();

		for (Map.Entry<ChunkCoordIntPair, Map<BlockPos, Integer>> chunkMap : lightPositions.entrySet()) {
			ChunkCoordIntPair coords = chunkMap.getKey();
			if (Math.abs(coords.chunkXPos - player().chunkCoordX) > range.get()
				|| Math.abs(coords.chunkZPos - player().chunkCoordZ) > range.get())
				continue;

			for (Map.Entry<BlockPos, Integer> entry : chunkMap.getValue().entrySet()) {
				BlockPos pos = entry.getKey();
				AxisAlignedBB bb = new AxisAlignedBB(pos, pos.add(1, 0, 1)).offset(0, 0.01, 0);

				GL11.glColor4f(195 / 255f, entry.getValue() * 13 / 255f, 0, 1);
				bb = bb.offset(-cam.x, -cam.y, -cam.z);

				worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION);

				GlStateManager.enableBlend();
				GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				GlStateManager.disableTexture2D();

				worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
				worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
				worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
				worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
				tessellator.draw();
			}

			GlStateManager.disableBlend();
			GlStateManager.enableTexture2D();
		}
	}

	private enum RenderMode {
		PLANES("Farben"),
		NUMBERS("Zahlen");

		private final String name;

		RenderMode(String name) {
			this.name = name;
		}
	}

}
