/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.qr_code_scanner;

import com.google.zxing.Binarizer;
import com.google.zxing.LuminanceSource;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates a BitMap from the two colors used the most.
 */
public class AggressiveBinarizer extends Binarizer {

	private final BitMatrix matrix;

	public static AggressiveBinarizer[] get(BufferedImage img) {
		int[] colors = new int[128 * 128];
		for (int y = 0; y < 128; y++)
			for (int x = 0; x < 128; x++)
				colors[y * 128 + x] = img.getRGB(x, y);

		HashMap<Integer, AtomicInteger> usedColors = new HashMap<>();
		for (int color : colors)
			usedColors.computeIfAbsent(color, k -> new AtomicInteger()).incrementAndGet();

		if (usedColors.size() == 1)
			return null;

		Map.Entry<Integer, AtomicInteger> mostUsedColor = null;
		Map.Entry<Integer, AtomicInteger>  secondColor = null;

		// Get most and second-most used colors
		for (Map.Entry<Integer, AtomicInteger> color : usedColors.entrySet()) {
			if (mostUsedColor == null) {
				mostUsedColor = color;
				continue;
			}

			int uses = color.getValue().get();

			if (secondColor == null) {
				if (mostUsedColor.getValue().get() < uses) {
					secondColor = mostUsedColor;
					mostUsedColor = color;
				} else {
					secondColor = color;
				}
				continue;
			}

			if (secondColor.getValue().get() < uses) {
				if (mostUsedColor.getValue().get() < uses) {
					secondColor = mostUsedColor;
					mostUsedColor = color;
					continue;
				}

				secondColor = color;
			}
		}

		//noinspection DataFlowIssue
		return new AggressiveBinarizer[] {
			new AggressiveBinarizer(colors, mostUsedColor.getKey()),
			new AggressiveBinarizer(colors, secondColor.getKey()),
		};
	}

	private AggressiveBinarizer(int[] img, int mainColor) {
		super(null);
		this.matrix = new BitMatrix(128);

		for (int y = 0; y < 128; y++)
			for (int x = 0; x < 128; x++)
				if (img[y * 128 + x] == mainColor)
					matrix.set(x, y);
	}

	@Override
	public BitArray getBlackRow(int y, BitArray row) {
		return matrix.getRow(y, row);
	}

	@Override
	public BitMatrix getBlackMatrix() {
		return matrix;
	}

	@Override
	public Binarizer createBinarizer(LuminanceSource source) {
		return null;
	}

}
