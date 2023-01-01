/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.world.furniture.util;

import dev.l3g7.griefer_utils.features.world.furniture.block.beacon.BeaconColor;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;

public enum BeaconVersionColor {

	DEFAULT(BeaconColor.DEFAULT, EnumDyeColor.BLUE),
	ORANGE(BeaconColor.ORANGE, EnumDyeColor.ORANGE),
	PURPLE(BeaconColor.PURPLE, EnumDyeColor.PURPLE),
	BLACK(BeaconColor.BLACK, EnumDyeColor.BLACK),
	RAINBOW(BeaconColor.RAINBOW, EntitySheep.getDyeRgb(EnumDyeColor.BLACK));

	private final BeaconColor color;
	private final float[] rgbColor;
	private final boolean isRainbow;

	BeaconVersionColor(BeaconColor beaconColor, EnumDyeColor EnumDyeColor) {
		color = beaconColor;
		rgbColor = EntitySheep.getDyeRgb(EnumDyeColor);
		isRainbow = false;
	}

	BeaconVersionColor(BeaconColor color, float[] rgbColor) {
		this.color = color;
		this.rgbColor = rgbColor;
		this.isRainbow = true;
	}

	public static BeaconVersionColor of(BeaconColor beaconColor) {
		for (BeaconVersionColor value : values())
			if (value.color.equals(beaconColor))
				return value;

		return BeaconVersionColor.DEFAULT;
	}

	public BeaconColor getColor() {
		return color;
	}

	public float[] getRgbColor() {
		return rgbColor;
	}

	public boolean isRainbow() {
		return isRainbow;
	}

}
