/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.util.render.RenderUtil;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import org.lwjgl.opengl.GL11;

import java.util.Map;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

class RenderChunkRenderer {

	private static void checkRecompile(boolean forceRecompilation) {
		for (RenderChunkManager manager : RenderChunkManager.managers)
			manager.checkRecompilation(forceRecompilation);
	}

	@EventListener
	private static void onRenderWorldLast(RenderWorldLastEvent event) {
		emptyCheck: {
			for (RenderChunkManager manager : RenderChunkManager.managers)
				if (!manager.chunks.isEmpty())
					break emptyCheck;

			return;
		}

		Profiler profiler = mc().mcProfiler;

		profiler.startSection("griefer_utils_render_engine");

		profiler.startSection("check_recompile");
		checkRecompile(false);
		if (AtlasManager.isOutdated()) {
			AtlasManager.recompile();
			checkRecompile(true);
		}

		profiler.endStartSection("setup_gl");
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
		GlStateManager.depthMask(true);
		GlStateManager.color(1, 1, 1, 1);

		profiler.endStartSection("frustum");
		double viewY = 360 - mc().getRenderManager().playerViewY + 12.5d;
		while (viewY < 0)
			viewY += 360;

		int rotationIndex = MathHelper.clamp_int((int) (Math.floor(viewY / 22.5) % 16), 0, 15);
		Frustum frustum = new Frustum();
		Vec3d cam = RenderUtil.camPos();
		frustum.setPosition(cam.x, cam.y, cam.z);

		profiler.endStartSection("render_chunks");
		for (RenderChunkManager manager : RenderChunkManager.managers) {
			profiler.startSection(manager.getClass().getSimpleName());

			for (Map.Entry<ChunkCoordIntPair, RenderChunk> entry : manager.chunks.entrySet()) {
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
				GlStateManager.translate(pair.chunkXPos * 16 - EntityFX.interpPosX,
					-EntityFX.interpPosY, pair.chunkZPos * 16 - EntityFX.interpPosZ);
				mc().mcProfiler.startSection("render_chunk");

				entry.getValue().draw(pair, frustum, rotationIndex);

				mc().mcProfiler.endSection();
				GlStateManager.popMatrix();
			}

			profiler.endSection();
		}

		profiler.endStartSection("teardown");
		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.disableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.enableCull();
		profiler.endSection();

		profiler.endSection();
	}

}
