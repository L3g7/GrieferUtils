/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building.redstone_helper;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.events.render.RefreshResourcesEvent;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureAtlas.FontAtlas;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.Vertices;

import java.util.HashMap;
import java.util.Map;


class FontRenderData {

	@SuppressWarnings("UnnecessaryUnicodeEscape") // (was just copied from Minecraft's FontRenderer)
	private static final String ASCII_GLYPHS = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";

	private static FontAtlas asciiAtlas = null;
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

		if (asciiAtlas == null)
			asciiAtlas = Util.tryFatal(() -> new FontAtlas("ascii"));


		TextureCutout texture = asciiAtlas.getSprite(indexInTexture);

		double scale = 4;
		double width = (texture.maxX() - texture.minX()) * scale / asciiAtlas.textureWidth;
		double height = (texture.maxY() - texture.minY()) * scale / asciiAtlas.textureHeight;
		return CharRenderInfo.of(texture, Vertices.onYPlane(width, height), 16);

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

			charStart -= (currentChar.width() / 2d); // Move the vertices so that minU is 0
			seperatedChars[i] = CharRenderInfo.of(
				currentChar.texture,
				baseVertices.translate(charStart, 0, 0),
				rotations
			);

			charStart -= (currentChar.width() / 2d);
			charStart -= SPACE_WIDTH;
		}

		return seperatedChars;
	}

	@EventListener
	private static void onRefresh(RefreshResourcesEvent event) {
		asciiAtlas = null;
		charData.clear();
		stringData.clear();
	}

	public record CharRenderInfo(TextureCutout texture, Vertices[] rotatedVertices) {

		static CharRenderInfo of(TextureCutout texture, Vertices baseVertices, int rotations) {
			Vertices[] rotatedVertices = new Vertices[rotations];
			rotatedVertices[0] = baseVertices;

			double singleRotation = 360 / (double) rotations;

			for (int i = 1; i < rotations; i++) {
				double rotation = Math.toRadians(-i * singleRotation);
				rotatedVertices[i] = baseVertices.rotateY((float) rotation);
			}

			return new CharRenderInfo(texture, rotatedVertices);
		}

		double width() {
			return rotatedVertices[0].b().xCoord - rotatedVertices[0].a().xCoord;
		}

	}

}