package dev.l3g7.griefer_utils.core.util.render.render_engine;

import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.UV;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.Vertices;
import net.minecraft.util.Vec3;

import static dev.l3g7.griefer_utils.core.util.render.render_engine.AtlasManager.getAtlas;
import static org.lwjgl.opengl.GL11.*;

public class ChunkCompiler {

	void bindTexture() {
		getAtlas().bind();
	}

	void begin(int list) {
		glNewList(list, GL_COMPILE);
		glBegin(GL_QUADS);
	}

	void end() {
		glEnd();
		glEndList();
	}

	public void render(TextureCutout cutout, Vertices vertices) {
		AtlasManager.register(cutout);
		if (!AtlasManager.isOutdated())
			render(AtlasManager.getAtlas().getUV(cutout), vertices);
	}

	public void render(UV uv, Vertices vertices) {
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
