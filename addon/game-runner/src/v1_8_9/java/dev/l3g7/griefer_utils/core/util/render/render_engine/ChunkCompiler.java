package dev.l3g7.griefer_utils.core.util.render.render_engine;

import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.UV;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.Vertices;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.function.Function;

import static org.lwjgl.opengl.GL11.*;

public class ChunkCompiler {


	private Function<Vertices, Vertices> transformation;
	private BlockPos blockPos;
	private Color currentColor = Color.WHITE;

	void prepareForRenderObject(BlockPos blockPos) {
		transformation = null;
		setColor(Color.WHITE);
		this.blockPos = blockPos;
	}

	void begin(int list) {
		glNewList(list, GL_COMPILE);
		glBegin(GL_QUADS);
	}

	public void end() {
		setColor(Color.WHITE);
		glEnd();
		glEndList();
	}

	public void setTransformation(Function<Vertices, Vertices> transformation) {
		this.transformation = transformation;
	}

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

	public void render(TextureCutout cutout, Vertices... allVertices) {
		AtlasManager.register(cutout);
		if (AtlasManager.isOutdated())
			return;

		UV uv = AtlasManager.getAtlas().getUV(cutout);
		for (Vertices vertices : allVertices)
			render(vertices, uv);
	}

	public void render(TextureCutout cutout, Vertices vertices) {
		AtlasManager.register(cutout);
		if (AtlasManager.isOutdated())
			return;

		UV uv = AtlasManager.getAtlas().getUV(cutout);
		render(vertices, uv);
	}

	private void render(Vertices vertices, UV uv) {
		if (transformation != null)
			vertices = transformation.apply(vertices);
		vertices = vertices.translate(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);

		vertex(vertices.a(), uv.maxU(), uv.maxV());
		vertex(vertices.b(), uv.minU(), uv.maxV());
		vertex(vertices.c(), uv.minU(), uv.minV());
		vertex(vertices.d(), uv.maxU(), uv.minV());
	}

	private void vertex(Vec3 pos, double u, double v) {
		glTexCoord2f((float) u, (float) v);
		glVertex3f((float) pos.xCoord, (float) pos.yCoord, (float) pos.zCoord);
	}

}
