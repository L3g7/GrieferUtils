package dev.l3g7.griefer_utils.core.util.render.render_engine.data;

import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public record Texture(ResourceLocation texture, double minU, double maxU, double minV, double maxV) {

	public static Texture of(ResourceLocation texture) {
		return new Texture(texture, 0, 1, 0, 1);
	}

	public double width() {
		return maxU - minU;
	}

	public double height() {
		return maxV - minV;
	}

	public void bind() {
		mc().getTextureManager().bindTexture(texture);
	}

}
