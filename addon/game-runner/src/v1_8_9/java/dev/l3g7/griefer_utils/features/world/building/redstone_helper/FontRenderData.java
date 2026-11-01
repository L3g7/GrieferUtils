/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building.redstone_helper;

import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;


public class FontRenderData {

	@SuppressWarnings("UnnecessaryUnicodeEscape") // (was just copied from Minecraft's FontRenderer)
	private static final String ASCII_GLYPHS = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";

	private static UVFinder uvFinder = null;
	private static final Map<Character, CharRenderInfo> charData = new HashMap<>();
	private static final Map<String, CharRenderInfo[]> stringData = new HashMap<>();

	public static CharRenderInfo[] getCharRenderInfos(String string) {
		return stringData.computeIfAbsent(string, key -> {
			CharRenderInfo[] allRenderInfos = key.chars()
				.mapToObj(c -> charData.computeIfAbsent((char) c, FontRenderData::computeRenderInfo))
				.toArray(CharRenderInfo[]::new);

			return separateVertices(allRenderInfos);
		});
	}

	private static CharRenderInfo computeRenderInfo(char c) {
		int indexInTexture = ASCII_GLYPHS.indexOf(c);
		if (indexInTexture == -1)
			throw new IllegalStateException("Char " + c + " is not in the ascii atlas, unicode pages are currently unsupported");

		if (uvFinder == null)
			uvFinder = UVFinder.newCharUVFinder("textures/font/ascii");
		return parseChar(uvFinder, indexInTexture);

	}

	private static CharRenderInfo parseChar(UVFinder uvFinder, int indexInTexture) {
		PixelUV uv = uvFinder.getUVData(indexInTexture);

		double scale = 2;
		double width = (uv.width() * scale) / uvFinder.texWidth;
		double height = (uv.height() * scale) / uvFinder.texHeight;
		return CharRenderInfo.of(uv.asTextureUV(uvFinder.texWidth, uvFinder.texHeight), new Vertices(
			new Vertex(-width, -height),
			new Vertex(width, -height),
			new Vertex(width, height),
			new Vertex(-width, height)
		), 16);
	}

	private static final double SPACE_WIDTH = 1 / 64d;

	private static CharRenderInfo[] separateVertices(CharRenderInfo[] chars) {
		if (chars.length < 2)
			return chars;

		double totalWidth = 0;
		int rotations = -1;

		for (CharRenderInfo aChar : chars) {
			if (totalWidth != 0)
				totalWidth += SPACE_WIDTH;

			totalWidth += aChar.width();

			if (rotations == -1)
				rotations = aChar.rotatedVertices.length;
			else if (rotations != aChar.rotatedVertices.length)
				throw new IllegalStateException("Different char rotations in one string");
		}

		CharRenderInfo[] seperatedChars = new CharRenderInfo[chars.length];

		double charStart = totalWidth / 2;

		for (int i = 0; i < chars.length; i++) {
			CharRenderInfo currentChar = chars[i];
			Vertices baseVertices = currentChar.rotatedVertices[0];

			charStart -= (currentChar.width() / 2d); // Move the vertices so that minX is 0
			seperatedChars[i] = CharRenderInfo.of(
				currentChar.textureUV,
				baseVertices.translateX(charStart),
				rotations
			);

			charStart -= (currentChar.width() / 2d);
			charStart -= SPACE_WIDTH;
		}

		return seperatedChars;
	}

	/**
	 * Determines the UV coordinates of a char
	 */
	private static class UVFinder {

		private final int[] pixels;
		private final int texWidth;
		private final int texHeight;

		private final int charWidth;
		private final int charHeight;

		private int x, y;

		private static UVFinder newCharUVFinder(String path) {
			try {
				InputStream stream = mc().getResourceManager().getResource(new ResourceLocation(path + ".png")).getInputStream();
				return new UVFinder(TextureUtil.readBufferedImage(stream));
				// TextureUtil.readBufferedImage closes the stream
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private UVFinder(BufferedImage img) {
			texWidth = img.getWidth();
			texHeight = img.getHeight();
			charHeight = texHeight / 16;
			charWidth = texWidth / 16;

			pixels = new int[texWidth * texHeight];
			img.getRGB(0, 0, texWidth, texHeight, pixels, 0, texWidth);
		}

		/**
		 * Determines the UV data of the given char by finding all edges
		 */
		private PixelUV getUVData(int indexInTexture) {
			this.x = (indexInTexture % 16) * charWidth;
			this.y = (indexInTexture / 16) * charHeight;

			return new PixelUV(
				/* minX */ x + findTextureEdge(charWidth, false, this::isColumnEmpty),
				/* maxX */ x + findTextureEdge(charWidth, true, this::isColumnEmpty),
				/* minY */ y + findTextureEdge(charHeight, false, this::isLineEmpty),
				/* maxY */ y + findTextureEdge(charHeight, true, this::isLineEmpty)
			);
		}

		private int findTextureEdge(int end, boolean reversed, Predicate<Integer> check) {
			if (!reversed)
				for (int i = 0; i < end; i++)
					if (!check.test(i))
						return i;

			for (int i = end - 1; i >= 0; i--)
				if (!check.test(i))
					return i + 1;

			throw new IllegalStateException(String.format("char at %d %d is invisible!", x, y));
		}

		private boolean isColumnEmpty(int x) {
			for (int i = 0; i < charHeight; i++)
				if (!isPixelEmpty(this.x + x, this.y + i))
					return false;

			return true;
		}

		private boolean isLineEmpty(int y) {
			for (int i = 0; i < charWidth; i++)
				if (!isPixelEmpty(this.x + i, this.y + y))
					return false;

			return true;
		}

		private boolean isPixelEmpty(int x, int y) {
			return (pixels[x + y * texWidth] >> 24) == 0;
		}

	}

	public static void reset() {
		uvFinder = null;
		charData.clear();
		stringData.clear();
	}

	@Mixin(Minecraft.class)
	private static class MixinMinecraft {

		@Inject(method = "refreshResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/IReloadableResourceManager;reloadResources(Ljava/util/List;)V", shift = AFTER))
		private void injectRefreshResources(CallbackInfo ci) {
			reset();
		}

	}

	public record CharRenderInfo(TextureUV textureUV, Vertices[] rotatedVertices) {

		static CharRenderInfo of(TextureUV textureUV, Vertices baseVertices, int rotations) {
			Vertices[] rotatedVertices = new Vertices[rotations];
			rotatedVertices[0] = baseVertices;

			double singleRotation = 360 / (double) rotations;

			for (int i = 1; i < rotations; i++) {
				double rotation = Math.toRadians(i * singleRotation);
				rotatedVertices[i] = baseVertices.rotate(rotation);
			}

			return new CharRenderInfo(textureUV, rotatedVertices);
		}

		double width() {
			return rotatedVertices[0].topRight.x - rotatedVertices[0].topLeft.x;
		}

	}

	/**
	 * The uv coordinates of a char, as positions from 0 to 1 on the atlas
	 */
	public record TextureUV(double minX, double maxX, double minY, double maxY) {}

	/**
	 * The uv coordinates of a char, in pixels on the atlas
	 */
	public record PixelUV(int minX, int maxX, int minY, int maxY) {

		public int width() {
			return maxX - minX;
		}

		public int height() {
			return maxY - minY;
		}

		public TextureUV asTextureUV(double textureWidth, double textureHeight) {
			return new TextureUV(
				this.minX / textureWidth,
				this.maxX / textureWidth,
				this.minY / textureHeight,
				this.maxY / textureHeight
			);
		}

	}

	public record Vertices(Vertex topLeft, Vertex topRight, Vertex bottomRight, Vertex bottomLeft) {

		public Vertices rotate(double by) {
			return map(v -> v.rotate(by));
		}

		public Vertices translateX(double deltaX) {
			return map(v -> new Vertex(v.x + deltaX, v.y));
		}

		private Vertices map(Function<Vertex, Vertex> mappingFunction) {
			return new Vertices(
				mappingFunction.apply(topLeft),
				mappingFunction.apply(topRight),
				mappingFunction.apply(bottomRight),
				mappingFunction.apply(bottomLeft)
			);
		}

	}

	/**
	 * (0, 0) is the middle of the block
	 */
	public record Vertex(double x, double y) {

		public Vertex rotate(double by) {
			double cos = Math.cos(by);
			double sin = Math.sin(by);
			return new Vertex(
				x * cos - y * sin,
				x * sin + y * cos
			);
		}

	}

}