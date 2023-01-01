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

package dev.l3g7.griefer_utils.features.world.furniture;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.world.furniture.block.ModBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static dev.l3g7.griefer_utils.util.misc.Constants.FURNITURE_RESOURCE_PACK_DIR;

public class ModBlocks {

	private static final Map<BlockKey, ModBlock> BLOCKS = new LinkedHashMap<>();

	public static ModBlock getBlockByKey(String key) {
		return BLOCKS.entrySet().stream()
				.filter(entry -> entry.getKey().getLocation().equals(key))
				.map(Map.Entry::getValue)
				.findFirst().orElse(null);
	}

	private static BlockKey getBlockKey(int idOffset, String name) {
		return new BlockKey(idOffset + 2500, name);
	}

	public static Map<BlockKey, ModBlock> getBlocks() {
		return BLOCKS;
	}

	public static void loadBlocks() {
		ArrayList<JsonObject> blocks = new ArrayList<>();
		Gson gson = new Gson();

		File default_blocks = new File(FURNITURE_RESOURCE_PACK_DIR, "customblocks/default_blocks.json");

		try (InputStreamReader inputStreamReader = new InputStreamReader(Files.newInputStream(default_blocks.toPath()), StandardCharsets.UTF_8)) {
			gson.fromJson(inputStreamReader, JsonArray.class)
					.forEach(e -> blocks.add(e.getAsJsonObject()));
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		blocks.sort(Comparator.comparingInt(o -> o.get("id").getAsInt()));
		ArrayList<ModBlock> modBlocks = new ArrayList<>();

		for (JsonObject block : blocks) {
			JsonObject properties = block.getAsJsonObject("properties");
			int id = block.get("id").getAsInt();
			String key = "griefer_utils:" + block.get("key").getAsString();

			try {
				Class<?> blockClass;
				try {
					blockClass = Class.forName("dev.l3g7.griefer_utils.features.world.furniture.block." + block.get("blockClass").getAsString());
				} catch (ClassNotFoundException cnfe) {
					System.err.println("BlockClass not found for " + key);
					continue;
				}

				BlockProperties blockProperties = BlockProperties.fromJson(properties);
				JsonObject blockData = new JsonObject();
				if (properties.has("data"))
					blockData = properties.get("data").getAsJsonObject();

				ModBlock modBlock = (ModBlock) blockClass.getConstructor(BlockProperties.class).newInstance(blockProperties);
				modBlock.setBlockData(blockData);

				if (block.has("versionBlockClass"))
					modBlock.setVersionBlockClass(block.get("versionBlockClass").getAsString());

				if(modBlock.getVersionBlockClass() != null) {
					try {
						Class.forName("dev.l3g7.griefer_utils.features.world.furniture.block.version_specific." + modBlock.getVersionBlockClass());
					} catch (ClassNotFoundException cnfe) {
						System.err.println("VersionBlockClass not found for " + key);
						continue;
					}
				}

				BlockKey blockKey = ModBlocks.getBlockKey(id, key);
				modBlock.setBlockKey(blockKey);
				BLOCKS.put(blockKey, modBlock);
				modBlocks.add(modBlock);
			} catch (Exception exception) {
				System.err.println("Parsing failed with key: " + key);
				System.err.println(properties);
			}
		}

		for (ModBlock modBlock : modBlocks)
			modBlock.initBlockData(modBlock.getBlockData());
	}

	static {
		loadBlocks();
	}

	public static class BlockKey {
		private final int id;
		private final String location;

		public BlockKey(int id, String location) {
			this.id = id;
			this.location = location;
		}

		public int getId() {
			return this.id;
		}

		public String getLocation() {
			return this.location;
		}

		public boolean equals(Object o) {
			if (o == this)
				return true;

			if (!(o instanceof BlockKey))
				return false;

			BlockKey other = (BlockKey) o;

			if (this.getId() != other.getId())
				return false;

			return Objects.equals(getLocation(), other.getLocation());
		}

		public int hashCode() {
			int result = 59 * (59 + getId());
			return result + (getLocation() == null ? 43 : getLocation().hashCode());
		}

	}

}
