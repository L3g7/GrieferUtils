package dev.l3g7.griefer_utils.core.util.render.render_engine.data;

import net.minecraft.client.renderer.GlStateManager;

import static dev.l3g7.griefer_utils.core.util.render.render_engine.RenderChunk.ROTATION_STEPS;

public enum RenderType {

	UNTEXTURED_DEPTHLESS(1, () -> {
		GlStateManager.disableDepth();
		GlStateManager.disableTexture2D();
	}),
	TEXTURED(1, () -> {
		GlStateManager.enableDepth();
		GlStateManager.enableTexture2D();
	}),
	TEXTURED_ROTATING(ROTATION_STEPS, () -> {
		GlStateManager.enableDepth();
		GlStateManager.enableTexture2D();
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
