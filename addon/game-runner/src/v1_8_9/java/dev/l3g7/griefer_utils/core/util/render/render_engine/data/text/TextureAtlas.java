/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine.data.text;

import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

/**
 * Reads individual textures out of an atlas as {@link TextureCutout}s.
 * @see FontAtlas
 */
public class TextureAtlas {

	protected final ResourceLocation texture;
	protected final int[] pixels;
	final int textureWidth;
	final int textureHeight;
	protected final int entriesX;
	protected final int entriesY;
	protected final int entryWidth;
	protected final int entryHeight;

	protected int x, y;

	/**
	 * Creates a new TextureAtlas.
	 * @param texture The atlas' texture.
	 * @param entriesX The amount of entries on the x-axis
	 * @param entriesY The amount of entries on the y-axis
	 * @throws IOException If the texture can't be loaded
	 */
	public TextureAtlas(ResourceLocation texture, int entriesX, int entriesY) throws IOException {
		InputStream stream = mc().getResourceManager().getResource(texture).getInputStream();
		BufferedImage img = TextureUtil.readBufferedImage(stream);
		// TextureUtil.readBufferedImage closes the stream

		this.texture = texture;
		textureWidth = img.getWidth();
		textureHeight = img.getHeight();
		this.entriesX = entriesX;
		this.entriesY = entriesY;
		entryHeight = textureHeight / entriesX;
		entryWidth = textureWidth / entriesY;

		pixels = new int[textureWidth * textureHeight];
		img.getRGB(0, 0, textureWidth, textureHeight, pixels, 0, textureWidth);
	}

	protected TextureCutout createSprite(int minX, int maxX, int minY, int maxY) {
		return new TextureCutout(
			texture,
			minX,
			minY, maxX,
			maxY
		);
	}

	/**
	 * Extracts the sprite at the given index
	 * @param spriteIndex The sprite's index
	 * @return The cutout sprite
	 */
	public TextureCutout getSprite(int spriteIndex) {
		this.x = (spriteIndex % entriesX) * entryWidth;
		this.y = (spriteIndex / entriesY) * entryHeight;

		return createSprite(x, x + entryWidth, y, y + entryHeight);
	}

}
