/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.util.render.RenderUtil;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import org.lwjgl.opengl.GL11;

import java.util.Map;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

class RenderChunkRenderer {

	/**
	 * Overwrites the currently loaded textures to prevent GlStateManager thinking textures.
	 */
	private static void resetTexture() {
		mc().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
	}

	@EventListener
	private static void onRenderWorldLast(RenderWorldLastEvent event) {
		if (RenderChunkManager.chunks.isEmpty())
			return;

		mc().mcProfiler.startSection("render_engine");

		if (Constants.SCHEMATICA) {
			mc().mcProfiler.startSection("update_schematic");
			SchematicaRenderChunkManager.checkForSchematicUpdate();
		}

		mc().mcProfiler.endStartSection("setup");
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
		GlStateManager.depthMask(true);
		resetTexture();
		GlStateManager.color(1, 1, 1, 1);

		double viewY = mc().getRenderManager().playerViewY + 12.5d;
		while (viewY < 0)
			viewY += 360;

		int rotationIndex = MathHelper.clamp_int((int) (Math.floor(viewY / 22.5) % 16), 0, 15);
		Frustum frustum = new Frustum();
		Vec3d cam = RenderUtil.camPos();
		frustum.setPosition(cam.x, cam.y, cam.z);

		mc().mcProfiler.endStartSection("render_chunks");
		renderChunks(RenderChunkManager.chunks, frustum, rotationIndex);
		if (Constants.SCHEMATICA) {
			mc().mcProfiler.endStartSection("render_chunks_schematic");
			renderChunks(SchematicaRenderChunkManager.schematicChunks, frustum, rotationIndex);
		}

		mc().mcProfiler.endStartSection("teardown");
		resetTexture();
		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.disableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.enableCull();
		mc().mcProfiler.endSection();

		mc().mcProfiler.endSection();
	}

	private static void renderChunks(Map<ChunkCoordIntPair, RenderChunk> chunks, Frustum frustum, int rotationIndex) {
		for (Map.Entry<ChunkCoordIntPair, RenderChunk> entry : chunks.entrySet()) {
			ChunkCoordIntPair pair = entry.getKey();
			boolean isVisible = frustum.isBoundingBoxInFrustum(new AxisAlignedBB(
				pair.getXStart(),
				0,
				pair.getZStart(),
				pair.getXEnd(),
				255,
				pair.getZEnd())
			);
			if (!isVisible)
				continue;

			GlStateManager.pushMatrix();
			GlStateManager.translate(entry.getKey().chunkXPos * 16 - EntityFX.interpPosX,
				-EntityFX.interpPosY, entry.getKey().chunkZPos * 16 - EntityFX.interpPosZ);
			mc().mcProfiler.startSection("render_chunk");

			entry.getValue().draw(entry.getKey(), frustum, rotationIndex);

			mc().mcProfiler.endSection();
			GlStateManager.popMatrix();
		}
	}

}
