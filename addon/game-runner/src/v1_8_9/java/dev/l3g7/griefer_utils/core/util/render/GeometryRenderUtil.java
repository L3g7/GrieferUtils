package dev.l3g7.griefer_utils.core.util.render;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.util.render.sinks.VertexSink;
import dev.l3g7.griefer_utils.core.util.render.sinks.WorldSink;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static net.minecraft.util.EnumFacing.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * A utility class for rendering geometry.
 */
public class GeometryRenderUtil {

	private static final WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();

	public static void drawBoxOutlines(AxisAlignedBB bb, Color color, float width) {
		float startX = (float) bb.minX;
		float startY = (float) bb.minY;
		float startZ = (float) bb.minZ;
		float endX = (float) bb.maxX;
		float endY = (float) bb.maxY;
		float endZ = (float) bb.maxZ;

		startLineDrawing(color, width);

		// Lower rectangle
		drawLineWhenRenderingStarted(startX, startY, startZ, startX, startY, endZ);
		drawLineWhenRenderingStarted(startX, startY, startZ, endX, startY, startZ);
		drawLineWhenRenderingStarted(endX, startY, startZ, endX, startY, endZ);
		drawLineWhenRenderingStarted(startX, startY, endZ, endX, startY, endZ);

		// upper rectangle
		drawLineWhenRenderingStarted(startX, endY, startZ, startX, endY, endZ);
		drawLineWhenRenderingStarted(startX, endY, startZ, endX, endY, startZ);
		drawLineWhenRenderingStarted(endX, endY, startZ, endX, endY, endZ);
		drawLineWhenRenderingStarted(startX, endY, endZ, endX, endY, endZ);

		// connecting lines
		drawLineWhenRenderingStarted(startX, startY, startZ, startX, endY, startZ);
		drawLineWhenRenderingStarted(startX, startY, endZ, startX, endY, endZ);
		drawLineWhenRenderingStarted(endX, startY, startZ, endX, endY, startZ);
		drawLineWhenRenderingStarted(endX, startY, endZ, endX, endY, endZ);

		finishLineDrawing();
	}

	public static void drawLine(BlockPos start, BlockPos end, Color color, float width) {
		drawLine(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ(), color, width);
	}

	public static void drawLine(float startX, float startY, float startZ, float endX, float endY, float endZ, Color color, float width) {
		startLineDrawing(color, width);
		drawLineWhenRenderingStarted(startX, startY, startZ, endX, endY, endZ);
		finishLineDrawing();
	}

	public static void startLineDrawing(Color color, float width) {
		// Update line width
		GL11.glLineWidth(width);
		GlStateManager.disableTexture2D();

		// Draw lines
		wr.begin(GL_LINES, DefaultVertexFormats.POSITION);
		GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

		GlStateManager.pushMatrix();
	}

	public static void finishLineDrawing() {
		GlStateManager.popMatrix();
		Tessellator.getInstance().draw();
		GlStateManager.enableTexture2D();
	}

	public static void drawLineWhenRenderingStarted(float startX, float startY, float startZ, float endX, float endY, float endZ) {
		Vec3 cam = RenderUtil.camPos();
		wr.pos(startX - cam.xCoord, startY - cam.yCoord, startZ - cam.zCoord).endVertex();
		wr.pos(endX - cam.xCoord, endY - cam.yCoord, endZ - cam.zCoord).endVertex();
	}

	public static void drawFilledBox(AxisAlignedBB bb, Color color, boolean drawInside) {
		Vec3 cam = RenderUtil.camPos();
		bb = bb.offset(-cam.xCoord, -cam.yCoord, -cam.zCoord);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION);

		GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
		GlStateManager.enableBlend();
		GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableTexture2D();

		Object blendState = Reflection.get(GlStateManager.class, "blendState");
		int originalSrcFactor = Reflection.get(blendState, "srcFactor");
		int originalDstFactor = Reflection.get(blendState, "dstFactor");
		GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		drawFilledBoxWhenRenderingStarted(WorldSink.INSTANCE, bb, drawInside);

		tessellator.draw();
		GlStateManager.blendFunc(originalSrcFactor, originalDstFactor);
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GL11.glColor4f(1f, 1f, 1f, 1f);
	}

	public static void drawFilledBoxWhenRenderingStarted(VertexSink sink, AxisAlignedBB bb, boolean drawInside) {
		drawFace(sink, bb, UP, drawInside);
		drawFace(sink, bb, DOWN, drawInside);
		drawFace(sink, bb, NORTH, drawInside);
		drawFace(sink, bb, SOUTH, drawInside);
		drawFace(sink, bb, WEST, drawInside);
		drawFace(sink, bb, EAST, drawInside);
	}

	public static void drawFace(VertexSink sink, AxisAlignedBB bb, EnumFacing face, boolean bothSides) {
		if (bothSides) {
			bb = new AxisAlignedBB(
				face == EAST ? bb.maxX : bb.minX,
				face == UP ? bb.maxY : bb.minY,
				face == SOUTH ? bb.maxZ : bb.minZ,
				face == WEST ? bb.minX : bb.maxX,
				face == DOWN ? bb.minY : bb.maxY,
				face == NORTH ? bb.minZ : bb.maxZ
			);
		}

		if (face.getAxis() == Axis.Y) {
			if (face == UP || bothSides) {
				sink.drawVertex(bb.minX, bb.maxY, bb.maxZ);
				sink.drawVertex(bb.maxX, bb.maxY, bb.maxZ);
				sink.drawVertex(bb.maxX, bb.maxY, bb.minZ);
				sink.drawVertex(bb.minX, bb.maxY, bb.minZ);
			}

			if (face == DOWN || bothSides) {
				sink.drawVertex(bb.maxX, bb.minY, bb.maxZ);
				sink.drawVertex(bb.minX, bb.minY, bb.maxZ);
				sink.drawVertex(bb.minX, bb.minY, bb.minZ);
				sink.drawVertex(bb.maxX, bb.minY, bb.minZ);
			}
			return;
		}

		if (face.getAxis() == Axis.X) {
			if (face == EAST || bothSides) {
				sink.drawVertex(bb.maxX, bb.maxY, bb.maxZ);
				sink.drawVertex(bb.maxX, bb.minY, bb.maxZ);
				sink.drawVertex(bb.maxX, bb.minY, bb.minZ);
				sink.drawVertex(bb.maxX, bb.maxY, bb.minZ);
			}

			if (face == WEST || bothSides) {
				sink.drawVertex(bb.minX, bb.minY, bb.maxZ);
				sink.drawVertex(bb.minX, bb.maxY, bb.maxZ);
				sink.drawVertex(bb.minX, bb.maxY, bb.minZ);
				sink.drawVertex(bb.minX, bb.minY, bb.minZ);
			}
			return;
		}

		if (face == SOUTH || bothSides) {
			sink.drawVertex(bb.maxX, bb.maxY, bb.maxZ);
			sink.drawVertex(bb.minX, bb.maxY, bb.maxZ);
			sink.drawVertex(bb.minX, bb.minY, bb.maxZ);
			sink.drawVertex(bb.maxX, bb.minY, bb.maxZ);
		}

		if (face == NORTH || bothSides) {
			sink.drawVertex(bb.minX, bb.maxY, bb.minZ);
			sink.drawVertex(bb.maxX, bb.maxY, bb.minZ);
			sink.drawVertex(bb.maxX, bb.minY, bb.minZ);
			sink.drawVertex(bb.minX, bb.minY, bb.minZ);
		}
	}
}
