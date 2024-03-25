package dev.l3g7.griefer_utils.features.world.redstone_helper;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.features.world.redstone_helper.RenderObjectObserver.Chunk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.Map;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {

	public static void render() {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);

		GlStateManager.depthMask(true);

		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/font/ascii.png"));

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.pushMatrix();
		GlStateManager.translate(-EntityFX.interpPosX, -EntityFX.interpPosY, -EntityFX.interpPosZ);

		double viewY = mc().getRenderManager().playerViewY + 12.5d;
		while (viewY < 0)
			viewY += 360;

		int rotationIndex = MathHelper.clamp_int((int) (Math.floor(viewY / 22.5) % 16), 0, 15);

		Frustum frustum = new Frustum();
		for (Map.Entry<ChunkCoordIntPair, Chunk> entry : RenderObjectObserver.data.entrySet()) {
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
		}

		GlStateManager.popMatrix();

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
			byteBuffer.limit(byteBuffer.capacity());
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
			ByteBuffer bytebuffer = GLAllocation.createDirectByteBuffer(newSize);
			this.byteBuffer.position(0);
			bytebuffer.put(this.byteBuffer);
			bytebuffer.rewind();
			this.byteBuffer = bytebuffer;
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

			/* preDraw */
			// preDraw Position
			byteBuffer.position(0);
			GL11.glVertexPointer(3, GL_FLOAT, offset, bytebuffer);
			GL11.glEnableClientState(GL_VERTEX_ARRAY);
			// preDraw UV
			byteBuffer.position(12);
			OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
			glTexCoordPointer(2, GL_FLOAT, offset, bytebuffer);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);

			// draw
			GL11.glDrawArrays(GL_QUADS, 0, vertexCount);

			/* postDraw */
			// Position
			glDisableClientState(GL_VERTEX_ARRAY);
			// postDraw UV
			OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
			OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
		}

	}
}
