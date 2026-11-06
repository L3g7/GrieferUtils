/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine.data.text;

import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * Reads glyphs out of Minecraft's atlas pages.
 * Removes padding on the x-axis by finding a glyphs edges.
 * @see TextGeometry
 */
public class FontAtlas extends TextureAtlas {

	/**
	 * Creates a new FontAtlas.
	 * @param pageName Which font page (texture) to use.
	 * @throws IOException If the texture can't be loaded
	 */
	public FontAtlas(String pageName) throws IOException {
		super(new ResourceLocation("minecraft", "textures/font/" + pageName + ".png"), 16, 16);
	}

	@Override
	public TextureCutout getSprite(int spriteIndex) {
		this.x = (spriteIndex % 16) * entryWidth;
		this.y = (spriteIndex / 16) * entryHeight;

		return createSprite(
			x + findTextureEdge(false),
			x + findTextureEdge(true),
			y,
			y + entryHeight
		);
	}

	private int findTextureEdge(boolean reversed) {
		if (!reversed) {
			for (int x = 0; x < entryWidth; x++)
				if (isColumnFilled(x))
					return x;
		} else {
			for (int x = entryWidth - 1; x >= 0; x--)
				if (isColumnFilled(x))
					return x + 1;
		}

		System.err.printf("Sprite at %d %d is invisible!%n", x, y);
		return reversed ? entryWidth : 0;
	}

	private boolean isColumnFilled(int x) {
		for (int i = 0; i < entryHeight; i++)
			if (isPixelFilled(this.x + x, this.y + i))
				return true;

		return false;
	}

	private boolean isPixelFilled(int x, int y) {
		return (pixels[x + y * textureWidth] >> 24) != 0;
	}

}
