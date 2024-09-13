/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.redstone_helper;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.features.world.redstone_helper.RenderObjectObserver.Chunk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL11;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {

	public static void render() {
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

		GlStateManager.pushMatrix();
		GlStateManager.translate(-EntityFX.interpPosX, -EntityFX.interpPosY, -EntityFX.interpPosZ);
		Frustum frustum = new Frustum();
		GlStateManager.popMatrix();

		for (Map.Entry<ChunkCoordIntPair, Chunk> entry : RenderObjectObserver.data.entrySet()) {
			GlStateManager.pushMatrix();

			GlStateManager.translate(entry.getKey().chunkXPos * 16 - EntityFX.interpPosX,
				-EntityFX.interpPosY, entry.getKey().chunkZPos * 16 - EntityFX.interpPosZ);
			ChunkCoordIntPair pair = entry.getKey();
			boolean isVisible = frustum.isBoundingBoxInFrustum(new AxisAlignedBB(
				pair.getXStart(),
				0,
				pair.getZStart(),
				pair.getXEnd(),
				255,
				pair.getZEnd())
			);

			if (isVisible)
				entry.getValue().draw(entry.getKey(), frustum, rotationIndex);

			GlStateManager.popMatrix();
		}

		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(516, 0.1F);
	}

	@EventListener
	private static void onRenderWorldLast(RenderWorldLastEvent event) {
		if (!RenderObjectObserver.isEnabled() || RenderObjectObserver.data.isEmpty())
			return;

		GlStateManager.disableCull();

		render();

		GlStateManager.disableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.enableCull();
	}

	public static class CompiledChunk {

		private ByteBuffer byteBuffer;
		private int vertexCount;
		private int vertexFormatIndex;
		private final VertexFormat vertexFormat = DefaultVertexFormats.POSITION_TEX;

		private boolean isDrawing;

		public CompiledChunk(int bytes) {
			this.byteBuffer = GLAllocation.createDirectByteBuffer(bytes);
			begin(); // TODO: remove
		}

		public void reset() {
			this.vertexCount = 0;
			this.vertexFormatIndex = 0;
		}

		public void begin() {
			if (this.isDrawing)
				throw new IllegalStateException("Already building!");

			isDrawing = true;
			reset();
			// Invoke limit of superclass because ByteBuffer#position doesn't exist in Java 8 // NOTE beautify
			((Buffer) byteBuffer).limit(byteBuffer.capacity());
		}

		public CompiledChunk tex(double u, double v) {
			int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.getOffset(vertexFormatIndex);

			this.byteBuffer.putFloat(i, (float) u);
			this.byteBuffer.putFloat(i + 4, (float) v);

			this.nextVertexFormatIndex();
			return this;
		}

		public void ensureCapacity() {
			int i = (vertexCount + 4) * vertexFormat.getNextOffset();
			if (i <= byteBuffer.capacity())
				return;

			int increase = 80 * 32;

			int newSize = byteBuffer.capacity() + increase;
			LogManager.getLogger().warn("[GU] Needed to grow BufferBuilder buffer: Old size " + byteBuffer.capacity() + " bytes, new size " + newSize + " bytes.");
			ByteBuffer buf = GLAllocation.createDirectByteBuffer(newSize);
			// Invoke position of superclass because ByteBuffer#position doesn't exist in Java 8
			((Buffer) this.byteBuffer).position(0);
			buf.put(this.byteBuffer);
			((Buffer) buf).position(0);
			this.byteBuffer = buf;
		}

		public void endVertex() {
			vertexCount++;
		}

		public CompiledChunk pos(double x, double y, double z) {
			int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.getOffset(vertexFormatIndex);

			this.byteBuffer.putFloat(i, (float) x);
			this.byteBuffer.putFloat(i + 4, (float) y);
			this.byteBuffer.putFloat(i + 8, (float) z);

			this.nextVertexFormatIndex();
			return this;
		}

		private void nextVertexFormatIndex() {
			vertexFormatIndex++;
			vertexFormatIndex %= vertexFormat.getElementCount();
		}

		public void draw() {
			ByteBuffer bytebuffer = byteBuffer;

			if (vertexCount <= 0)
				return;

			int offset = vertexFormat.getNextOffset();
			List<VertexFormatElement> elements = vertexFormat.getElements();

			// preDraw
			for (int i = 0; i < elements.size(); i++)
				preDraw(elements.get(i).getUsage(), vertexFormat, i, offset, bytebuffer);

			// draw
			GL11.glDrawArrays(GL_QUADS, 0, vertexCount);

			// postDraw
			for (int i = 0; i < elements.size(); i++)
				postDraw(elements.get(i).getUsage(), vertexFormat, i);
		}

	}

	public static void preDraw(VertexFormatElement.EnumUsage attrType, VertexFormat format, int element, int stride, ByteBuffer buffer) {
		VertexFormatElement attr = format.getElement(element);
		int count = attr.getElementCount();
		int constant = attr.getType().getGlConstant();
		// Invoke position of superclass because ByteBuffer#position doesn't exist in Java 8
		((Buffer) buffer).position(format.getOffset(element));
		switch (attrType) {
			case POSITION -> {
				glVertexPointer(count, constant, stride, buffer);
				glEnableClientState(GL_VERTEX_ARRAY);
			}
			case UV -> {
				OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + attr.getIndex());
				glTexCoordPointer(count, constant, stride, buffer);
				glEnableClientState(GL_TEXTURE_COORD_ARRAY);
				OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
			}
		}
	}

	public static void postDraw(VertexFormatElement.EnumUsage attrType, VertexFormat format, int element) {
		VertexFormatElement attr = format.getElement(element);
		switch (attrType) {
			case POSITION -> glDisableClientState(GL_VERTEX_ARRAY);
			case UV -> {
				OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + attr.getIndex());
				glDisableClientState(GL_TEXTURE_COORD_ARRAY);
				OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
			}
		}
	}

}
