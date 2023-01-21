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

package dev.l3g7.griefer_utils.features.world.furniture.download;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.util.misc.Constants.FURNITURE_RESOURCE_PACK_DIR;

public class ResourceRewriter {

	private static final Pattern MC_BLOCK_PATTERN = Pattern.compile("\"((minecraft:blocks|blocks)/(.*?))\"");
	private static final JsonParser JSON_PARSER = new JsonParser();
	private static final File ASSETS = new File(FURNITURE_RESOURCE_PACK_DIR, "assets");
	private static final File GRIEFER_UTILS = new File(ASSETS, "/griefer_utils");

	public static void rewrite() throws IOException {
		File[] cb = ASSETS.listFiles();

		if (cb == null)
			throw new IOException("Could not list assets");

		if (!cb[0].renameTo(GRIEFER_UTILS))
			throw new IOException("Could not rename resource domain");

		List<File> files = new ArrayList<>();
		addFiles(files, GRIEFER_UTILS);

		for (File file : files)
			rewriteFile(file);
	}

	@SuppressWarnings("ConstantConditions")
	private static void addFiles(List<File> files, File currentFile) {
		if (currentFile.getName().equals("sounds") || currentFile.getName().equals("textures"))
			return;

		if (currentFile.isFile()) {
			files.add(currentFile);
			return;
		}

		for (File file : currentFile.listFiles())
			addFiles(files, file);
	}

	private static void rewriteFile(File file) throws IOException {
		if (!file.getName().endsWith(".json"))
			return;

		String path = file.getAbsolutePath().replace('\\', '/');

		String pathPrefix = "furniture_resourcepack/assets/griefer_utils/";
		path = path.substring(path.indexOf(pathPrefix) + pathPrefix.length());

		if (path.startsWith("textures/") || path.startsWith("sounds/"))
			return;

		String content, oldJson;
		content = oldJson = Files.readAllLines(file.toPath()).stream().reduce((s, s2) -> s + "\n" + s2).orElse("");

		if (path.startsWith("blockstates/"))
			content = rewriteBlockstates(content);
		else if (path.startsWith("models/"))
			content = rewriteModels(content);
		else if (path.startsWith("lang/"))
			content = rewriteLang(content);

		content = content.replace("cb", "griefer_utils");

		if (oldJson.equals(content))
			return;

		Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));

		if (path.startsWith("lang/"))
			if (!file.renameTo(new File(file.getParentFile(), "en_us.lang")))
				throw new IOException("Could not rename language file");

		if (path.startsWith("models/block")) {
			File itemModel = new File(file.getParentFile().getParentFile().getAbsolutePath() + "/item/" + file.getName());
			if (itemModel.exists())
				return;

			Files.write(itemModel.toPath(), content.getBytes(StandardCharsets.UTF_8));
		}
	}

	private static String rewriteBlockstates(String jsonString) {
		JsonObject jsonObject = JSON_PARSER.parse(jsonString).getAsJsonObject();
		if (!jsonObject.has("multipart") && !jsonObject.has("variants"))
			return jsonString;

		if (jsonObject.has("multipart")) {
			for (JsonElement multipartVariantElement : jsonObject.getAsJsonArray("multipart")) {
				JsonObject multipartVariant = multipartVariantElement.getAsJsonObject();

				List<JsonObject> variantsToModify = new ArrayList<>();
				if (multipartVariant.get("apply").isJsonArray())
					for (JsonElement variant : multipartVariant.get("apply").getAsJsonArray())
						variantsToModify.add(variant.getAsJsonObject());
				else if (multipartVariant.get("apply").isJsonObject())
					variantsToModify.add(multipartVariant.getAsJsonObject("apply"));

				for (JsonObject variant : variantsToModify)
					variant.addProperty("model", variant.getAsJsonPrimitive("model").getAsString().replace("block/", ""));
			}
		}

		if (jsonObject.has("variants")) {
			JsonObject variants = jsonObject.getAsJsonObject("variants");
			for (Map.Entry<String, JsonElement> variantEntry : variants.entrySet()) {
				List<JsonObject> variantsToModify = new ArrayList<>();

				if (variantEntry.getValue().isJsonObject())
					variantsToModify.add(variantEntry.getValue().getAsJsonObject());
				else
					for (JsonElement variant : variantEntry.getValue().getAsJsonArray())
						variantsToModify.add(variant.getAsJsonObject());

				for (JsonObject variant2 : variantsToModify)
					variant2.addProperty("model", variant2.getAsJsonPrimitive("model").getAsString().replace("block/", ""));
			}

			if (variants.entrySet().size() == 1 && variants.entrySet().iterator().next().getKey().isEmpty()) {
				variants.add("normal", variants.entrySet().iterator().next().getValue());
				variants.remove("");
			}
		}

		return jsonObject.toString();
	}

	private static String rewriteModels(String jsonString) {
		jsonString = replace(jsonString,
				"\"firstperson_righthand\"", "\"firstperson\"",
				"\"thirdperson_righthand\"", "\"thirdperson\""
		);
		JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
		jsonReader.setLenient(true);
		JsonObject jsonObject = JSON_PARSER.parse(jsonReader).getAsJsonObject();

		if (jsonObject.has("parent") && jsonObject.get("parent").getAsString().startsWith("minecraft:")) {
			if (jsonObject.get("parent").getAsString().equals("minecraft:block/tinted_cross"))
				jsonObject.addProperty("parent", "griefer_utils:block/tinted_cross");
			jsonObject.addProperty("parent", jsonObject.get("parent").getAsString().replace("minecraft:", "mc:").replace("cube_column_horizontal", "column_side"));
		}

		if (jsonObject.has("display")) {
			JsonObject display = jsonObject.getAsJsonObject("display");

			if (display.has("gui"))
				rewriteDisplayMode(display, "gui");
			rewriteDisplayMode(display, "thirdperson");
			rewriteDisplayMode(display, "firstperson");

			if (display.has("ground") && display.getAsJsonObject("ground").has("scale")) {
				JsonArray currentScale = display.getAsJsonObject("ground").getAsJsonArray("scale");
				JsonArray newScale = makeJsonArray(currentScale.get(0).getAsFloat(), currentScale.get(1).getAsFloat(), currentScale.get(2).getAsFloat());
				newScale = mult(newScale, 4);
				display.getAsJsonObject("ground").add("scale", newScale);
			}
		}

		jsonString = jsonObject.toString();

		// Wood
		for (String type : new String[]{"oak", "spruce", "birch", "jungle", "acacia"}) {
			jsonString = replace(jsonString,
					"/" + type + "_log\"", "/log_" + type + "\"",
					"/" + type + "_log_top\"", "/log_" + type + "_top\"",
					"/" + type + "_planks\"", "/planks_" + type + "\"",
					"/" + type + "_leaves\"", "/leaves_" + type + "\""
			);
		}

		// Colored blocks
		for (String color : new String[]{"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "cyan", "purple", "blue", "brown", "green", "red", "black"}) {
			jsonString = replace(jsonString,
					"/" + color + "_terracotta\"", "/hardened_clay_stained_" + color + "\"",
					"/" + color + "_wool\"", "/wool_colored_" + color + "\""
			);
		}

		jsonString = replace(jsonString,
				"\"block/", "\"blocks/",
				"\"minecraft:block/", "\"minecraft:blocks/",
				"\"mc:block/", "\"minecraft:block/",
				"/anvil\"", "/anvil_base\"",
				"/mushroom_stem\"", "/mushroom_block_skin_stem\"",
				"/dark_oak_log\"", "/log_big_oak\"",
				"/dark_oak_planks\"", "/planks_big_oak\"",
				"/dark_oak_leaves\"", "/leaves_big_oak\"",
				"/light_gray_terracotta\"", "/hardened_clay_stained_silver\"",
				"/light_gray_wool\"", "/wool_colored_silver\""
		);

		Matcher matcher = MC_BLOCK_PATTERN.matcher(jsonString);
		while (matcher.find()) {
			String blockName = matcher.group(3);
			if (!new File(ASSETS, "/griefer_utils/textures/new_blocks/" + blockName + ".png").isFile())
				continue;

			blockName += "\"";
			String newLocation = "\"griefer_utils:new_blocks/" + blockName;
			jsonString = replace(jsonString,
					"\"minecraft:blocks/" + blockName, newLocation,
					"\"blocks/" + blockName, newLocation
			);
		}

		return jsonString;
	}

	private static String rewriteLang(String jsonString) {
		JsonObject jsonObject = JSON_PARSER.parse(jsonString).getAsJsonObject();
		StringBuilder text = new StringBuilder();

		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String key = entry.getKey();
			String newKey = key;

			if (key.equals("itemGroup.cb"))
				newKey = "itemGroup.griefer_utils";
			else if (key.startsWith("block.cb"))
				newKey = "tile." + key.replace("block.cb.", "") + ".name";

			String value = key.equals("itemGroup.cb") ? "Möbel" : jsonObject.get(key).getAsString();

			text.append(newKey)
					.append("=")
					.append(value)
					.append(System.lineSeparator());
		}

		return text.toString();
	}

	private static String replace(String string, String... args) {
		for (int i = 0; i < args.length; )
			string = string.replace(args[i++], args[i++]);

		return string;
	}

	private static JsonArray mult(JsonArray currentElement, int scale) {
		float[] ret = new float[3];

		for (int i = 0; i < 3; i++)
			ret[i] = currentElement.get(i).getAsFloat() * scale;

		return makeJsonArray(ret[0], ret[1], ret[2]);
	}

	private static JsonArray makeJsonArray(float... values) {
		JsonArray array = new JsonArray();
		for (float value : values)
			array.add(new JsonPrimitive(value));

		return array;
	}

	private static void rewriteDisplayMode(JsonObject display, String viewMode) {
		if (!display.has(viewMode))
			return;

		JsonObject currentMode = display.getAsJsonObject(viewMode);
		JsonArray currentTranslation = currentMode.has("translation") ? currentMode.getAsJsonArray("translation") : makeJsonArray(0, 0, 0);
		JsonArray currentRotation = currentMode.has("rotation") ? currentMode.getAsJsonArray("rotation") : makeJsonArray(0, 0, 0);
		JsonArray currentScale = currentMode.has("scale") ? currentMode.getAsJsonArray("scale") : makeJsonArray(1, 1, 1);

		display.remove(viewMode);

		JsonObject returnDisplayObject = new JsonObject();
		if (currentMode.has("translation"))
			returnDisplayObject.add("translation", currentTranslation);
		if (currentMode.has("rotation"))
			returnDisplayObject.add("rotation", currentRotation);

		returnDisplayObject.add("scale", mult(currentScale, 2));
		display.add(viewMode, returnDisplayObject);
	}

}
