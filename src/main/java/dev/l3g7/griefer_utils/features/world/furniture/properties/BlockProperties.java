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

package dev.l3g7.griefer_utils.features.world.furniture.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class BlockProperties {
	public final Material material;
	public final MapColor materialColor;
	public final Block.SoundType soundType;
	public final float resistance;
	public final float hardness;
	public final boolean fullCube;

	private BlockProperties(Material material, MapColor materialColor, Block.SoundType soundType, float resistance, float hardness, boolean fullCube) {
		this.material = material;
		this.materialColor = materialColor;
		this.soundType = soundType;
		this.resistance = resistance;
		this.hardness = hardness;
		this.fullCube = fullCube;
	}

	public static BlockProperties fromJson(JsonObject object) {
		Material material = BlockPropertyConverter.getMaterial(object.get("material").getAsString());

		JsonElement materialColor = object.get("materialColor");
		MapColor mapColor = materialColor == null ? material.getMaterialMapColor() : BlockPropertyConverter.getMapColor(materialColor.getAsString());

		Block.SoundType soundType = BlockPropertyConverter.getSoundType(object.get("soundType").getAsString());
		Hardness hardness = BlockPropertyConverter.getHardness(object.get("hardness"));
		boolean fullCube = object.has("fullCube") && object.get("fullCube").getAsBoolean();

		return new BlockProperties(material, mapColor, soundType, hardness.hardness, hardness.resistance, fullCube);
	}

	public static class Hardness {

		private final float hardness;
		private final float resistance;

		public Hardness(float hardness, float resistance) {
			this.hardness = hardness;
			this.resistance = resistance;
		}

		public float getHardness() {
			return hardness;
		}

		public float getResistance() {
			return resistance;
		}

	}

}
