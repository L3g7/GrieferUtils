package dev.l3g7.griefer_utils.core.util.render.render_engine.data;

import net.minecraft.util.ResourceLocation;

public record TextureCutout(ResourceLocation texture, int minX, int minY, int maxX, int maxY) {

	public TextureCutout(String domain, String path) {
		this(new ResourceLocation(domain, path));
	}

	public TextureCutout(ResourceLocation location) {
		this(location, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

}
