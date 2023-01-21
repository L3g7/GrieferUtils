/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties.Hardness;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

import java.util.Arrays;
import java.util.List;

public class BlockPropertyConverter {
	private static final List<String> COLORS = Arrays.asList("AIR", "GRASS", "SAND", "WOOL", "TNT", "ICE", "IRON", "FOLIAGE", "SNOW", "CLAY", "DIRT", "STONE", "WATER", "WOOD", "QUARTZ", "ADOBE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY", "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK", "GOLD", "DIAMOND", "LAPIS", "EMERALD", "OBSIDIAN", "NETHERRACK", "WHITE_TERRACOTTA", "ORANGE_TERRACOTTA", "MAGENTA_TERRACOTTA", "LIGHT_BLUE_TERRACOTTA", "YELLOW_TERRACOTTA", "LIME_TERRACOTTA", "PINK_TERRACOTTA", "GRAY_TERRACOTTA", "LIGHT_GRAY_TERRACOTTA", "CYAN_TERRACOTTA", "PURPLE_TERRACOTTA", "BLUE_TERRACOTTA", "BROWN_TERRACOTTA", "GREEN_TERRACOTTA", "RED_TERRACOTTA", "BLACK_TERRACOTTA", "CRIMSON_NYLIUM", "CRIMSON_STEM", "CRIMSON_HYPHAE", "WARPED_NYLIUM", "WARPED_STEM", "WARPED_HYPHAE", "WARPED_WART");

	public static final Material STONE = new Material(MapColor.stoneColor);
	public static final Material WOOD = new Material(MapColor.woodColor);
	public static final Material GOLD = new Material(MapColor.goldColor);
	public static final Material WOOL = new Material(MapColor.clothColor);

	public static MapColor getMapColor(String name) {
		int index = COLORS.indexOf(name);
		if (index == -1)
			return null;

		MapColor color = MapColor.mapColorArray[index];
		return color == null ? MapColor.stoneColor : color;
	}

	public static Material getMaterial(String name) {
		switch (name) {
			case "WOOD":           return WOOD;
			case "STONE":          return STONE;
			case "STONE_ORIGINAL": return Material.rock;
			case "WOOL":           return WOOL;
			case "IRON":           return Material.iron;
			case "ANVIL":          return Material.anvil;
			case "LEAVES":         return Material.leaves;
			case "GLASS":          return Material.glass;
			case "GOLD":           return GOLD;
			default:               return Material.wood;
		}
	}

	public static Block.SoundType getSoundType(String name) {
		switch (name) {
			case "WOOD":  return Block.soundTypeWood;
			case "ANVIL": return Block.soundTypeAnvil;
			case "CLOTH": return Block.soundTypeCloth;
			case "METAL": return Block.soundTypeMetal;
			case "PLANT": return Block.soundTypeGrass;
			case "GLASS": return Block.soundTypeGlass;
			default:      return Block.soundTypeStone;
		}
	}

	public static Hardness getHardness(JsonElement element) {
		if (element == null)
			return new Hardness(2f, 5f);

		switch (element.getAsString()) {
			case "LEAVES":      return new Hardness(0.2f, 1.0f);
			case "SEA_LANTERN": return new Hardness(0.3f, 1.5f);
			case "CARPET":      return new Hardness(0.1f, 1/6f);
			case "IRON_BLOCK":  return new Hardness(5.0f, 10.0f);
			case "GRASS":       return new Hardness(0.6f, 3.0f);
			case "LOG":         return new Hardness(2.0f, 10.0f);
			case "PLANT":       return new Hardness(0.0f, 0.0f);
			default:            return new Hardness(2.0f, 5.0f);
		}
	}

	static {
		Reflection.invoke(STONE, "setBurning");
	}
}
