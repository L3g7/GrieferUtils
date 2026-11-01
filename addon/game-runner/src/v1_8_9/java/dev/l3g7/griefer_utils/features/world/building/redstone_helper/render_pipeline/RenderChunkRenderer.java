/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building.redstone_helper.render_pipeline;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import org.lwjgl.opengl.GL11;

import java.util.Map;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

class RenderChunkRenderer {

	@EventListener
	private static void onRenderWorldLast(RenderWorldLastEvent event) {
		if (RenderChunkManager.data.isEmpty())
			return;

		Profiler profiler = mc().mcProfiler;

		profiler.startSection("render_chunk_renderer");
		profiler.startSection("setup");
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
		GlStateManager.depthMask(true);

		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/font/ascii.png"));

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		double viewY = mc().getRenderManager().playerViewY + 12.5d;
		while (viewY < 0)
			viewY += 360;

		int rotationIndex = MathHelper.clamp_int((int) (Math.floor(viewY / 22.5) % 16), 0, 15);

		Frustum frustum = new Frustum();

		profiler.endStartSection("render_chunks");
		for (Map.Entry<ChunkCoordIntPair, RenderChunk> entry : RenderChunkManager.data.entrySet()) {
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
			profiler.startSection("render_chunk");

			entry.getValue().draw(entry.getKey(), frustum, rotationIndex);

			profiler.endSection();
			GlStateManager.popMatrix();
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
