package dev.l3g7.griefer_utils.core.util.render.render_engine;

import dev.l3g7.griefer_utils.core.util.render.render_engine.data.Quad;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.UV;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.function.Function;

import static org.lwjgl.opengl.GL11.*;

public class ChunkCompiler {

	/**
	 * A transformation applied to every quad drawn.<br>
	 * <code>null</code> if no transformation has been set.
	 */
	private Function<Quad, Quad> transformation;

	/**
	 * The position of the RenderObject currently being drawing
	 */
	private BlockPos blockPos;
	private Color currentColor = Color.WHITE;

	/**
	 * Prepares the compiler for drawing a new {@link RenderObject}
	 * @param blockPos The RenderObject's positions
	 */
	void prepare(BlockPos blockPos) {
		transformation = null;
		setColor(Color.WHITE);
		this.blockPos = blockPos;
	}

	/**
	 * Begins drawing into a GL list.
	 * @param list The list's id.
	 */
	void begin(int list) {
		glNewList(list, GL_COMPILE);
		glBegin(GL_QUADS);
	}

	/**
	 * Ends drawing into the current GL list.
	 */
	void end() {
		setColor(Color.WHITE);
		glEnd();
		glEndList();
	}

	/**
	 * Sets the transformation applied to every quad drawn.<br>
	 * Only one transformation can be active at the same time.
	 */
	public void setTransformation(Function<Quad, Quad> transformation) {
		this.transformation = transformation;
	}

	/**
	 * Sets the current color.
	 * If it's not equal to the previous color, it'll be changed in GL too.
	 */
	public void setColor(Color color) {
		if (currentColor.equals(color))
			return;

		currentColor = color;
		GL11.glColor4f(
			color.getRed() / 255f,
			color.getGreen() / 255f,
			color.getBlue() / 255f,
			color.getAlpha() / 255f
		);
	}

	/**
	 * Draws multiple quads with the given texture
	 */
	public void draw(TextureCutout cutout, Quad... allVertices) {
		Atlas.register(cutout);
		if (Atlas.isOutdated())
			return;

		UV uv = Atlas.getAtlas().getUV(cutout);
		for (Quad quad : allVertices)
			draw(uv, quad);
	}

	/**
	 * Draws a quad with the given texture
	 */
	public void draw(TextureCutout cutout, Quad quad) {
		Atlas.register(cutout);
		if (Atlas.isOutdated())
			return;

		UV uv = Atlas.getAtlas().getUV(cutout);
		draw(uv, quad);
	}

	private void draw(UV uv, Quad quad) {
		if (transformation != null)
			quad = transformation.apply(quad);

		quad = quad.translate(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);

		drawVertex(quad.a(), uv.maxU(), uv.maxV());
		drawVertex(quad.b(), uv.minU(), uv.maxV());
		drawVertex(quad.c(), uv.minU(), uv.minV());
		drawVertex(quad.d(), uv.maxU(), uv.minV());
	}

	private void drawVertex(Vec3 pos, double u, double v) {
		glTexCoord2f((float) u, (float) v);
		glVertex3f((float) pos.xCoord, (float) pos.yCoord, (float) pos.zCoord);
	}

}
