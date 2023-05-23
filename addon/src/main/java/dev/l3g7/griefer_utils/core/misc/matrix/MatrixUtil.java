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

package dev.l3g7.griefer_utils.core.misc.matrix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.util.IOUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MatrixUtil {

	public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(16);

	/**
	 * Serializes an object into a signature payload.
	 */
	public static byte[] toSignaturePayload(Object obj) {
		JsonObject jsonObj = IOUtil.gson.toJsonTree(obj).getAsJsonObject();
		jsonObj.remove("signatures");
		jsonObj.remove("unsigned");
		return IOUtil.gson.toJson(toCanonicalJson(jsonObj)).getBytes(UTF_8);
	}

	/**
	 * Converts a json object into <a href="https://spec.matrix.org/v1.6/appendices/#canonical-json">canonical json</a>.
	 */
	private static JsonObject toCanonicalJson(JsonObject jsonObject) {
		List<String> keySet = jsonObject.entrySet().stream().map(Map.Entry::getKey).sorted().collect(Collectors.toList());
		JsonObject temp = new JsonObject();
		for (String key : keySet) {
			JsonElement value = jsonObject.get(key);
			if (value.isJsonObject())
				temp.add(key, toCanonicalJson(value.getAsJsonObject()));
			else
				temp.add(key, value);
		}
		return temp;
	}

}