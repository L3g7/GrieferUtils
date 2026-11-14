package dev.l3g7.griefer_utils.core.util.render.render_engine.data;

import org.lwjgl.opengl.GL11;

import static dev.l3g7.griefer_utils.core.util.render.render_engine.RenderChunk.ROTATION_STEPS;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

public enum RenderType {

	UNTEXTURED_DEPTHLESS(1, () -> {
		GL11.glDisable(GL_DEPTH_TEST);
		GL11.glDisable(GL_TEXTURE_2D);
	}),
	TEXTURED(1, () -> {
		GL11.glEnable(GL_DEPTH_TEST);
		GL11.glEnable(GL_TEXTURE_2D);
	}),
	TEXTURED_ROTATING(ROTATION_STEPS, () -> {
		GL11.glEnable(GL_DEPTH_TEST);
		GL11.glEnable(GL_TEXTURE_2D);
	});

	public final int size;
	private final Runnable setup;

	RenderType(int size, Runnable setup) {
		this.size = size;
		this.setup = setup;
	}

	public void setup() {
		this.setup.run();
	}

}
