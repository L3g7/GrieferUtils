package dev.l3g7.griefer_utils.core.util.render.render_engine.data;

import net.minecraft.util.ResourceLocation;

/**
 * A cutout / subimage of a texture.
 * @param minX,minY,maxX,maxY The cutout bounds in pixels.
 */
public record TextureCutout(ResourceLocation texture, int minX, int minY, int maxX, int maxY) {

	public TextureCutout(String namespace, String path) {
		this(new ResourceLocation(namespace, path));
	}

	public TextureCutout(ResourceLocation location) {
		this(location, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

}
