package dev.l3g7.griefer_utils.features.world.building.redstone_helper.render_pipeline;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL11;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class ChunkCompiler {

	private ByteBuffer byteBuffer;
	private int vertexCount;
	private int vertexFormatIndex;
	private final VertexFormat vertexFormat = DefaultVertexFormats.POSITION_TEX;

	private boolean isDrawing;

	public ChunkCompiler(int bytes) {
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

	public ChunkCompiler tex(double u, double v) {
		int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.getOffset(vertexFormatIndex);

		this.byteBuffer.putFloat(i, (float) u);
		this.byteBuffer.putFloat(i + 4, (float) v);

		this.nextVertexFormatIndex();
		return this;
	}

	public ChunkCompiler pos(double x, double y, double z) {
		int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.getOffset(vertexFormatIndex);

		this.byteBuffer.putFloat(i, (float) x);
		this.byteBuffer.putFloat(i + 4, (float) y);
		this.byteBuffer.putFloat(i + 8, (float) z);

		this.nextVertexFormatIndex();
		return this;
	}

	public void ensureCapacity() {
		// TODO: this shouldn't be needed
		int i = (vertexCount + 4) * vertexFormat.getNextOffset();
		if (i <= byteBuffer.capacity())
			return;

		int newSize = Math.max(byteBuffer.capacity() * 2, i);
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
			preDraw(elements.get(i).getUsage(), i, offset, bytebuffer);

		// draw
		GL11.glDrawArrays(GL_QUADS, 0, vertexCount);

		// postDraw
		for (int i = 0; i < elements.size(); i++)
			postDraw(elements.get(i).getUsage(), i);
	}

	public void preDraw(VertexFormatElement.EnumUsage attrType, int element, int stride, ByteBuffer buffer) {
		VertexFormatElement attr = vertexFormat.getElement(element);
		int count = attr.getElementCount();
		int constant = attr.getType().getGlConstant();
		// Invoke position of superclass because ByteBuffer#position doesn't exist in Java 8
		((Buffer) buffer).position(vertexFormat.getOffset(element));
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

	public void postDraw(VertexFormatElement.EnumUsage attrType, int element) {
		VertexFormatElement attr = vertexFormat.getElement(element);
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
