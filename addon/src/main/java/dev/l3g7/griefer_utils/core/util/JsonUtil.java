/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.UUID;

public class JsonUtil {

	public static JsonObject jsonObject(Object... data) {
		JsonObject obj = new JsonObject();

		for (int i = 0; i < data.length; i += 2)
			obj.add((String) data[i], element(data[i + 1]));

		return obj;
	}

	private static JsonElement element(Object data) {
		if (data == null)
			return JsonNull.INSTANCE;
		if (data instanceof JsonElement)
			return (JsonElement) data;
		if (data instanceof String)
			return new JsonPrimitive((String) data);
		else if (data instanceof Number)
			return new JsonPrimitive((Number) data);
		else if (data instanceof Boolean)
			return new JsonPrimitive((Boolean) data);
		else if (data instanceof Character)
			return new JsonPrimitive((Character) data);

		else if (data instanceof UUID)
			return new JsonPrimitive(((UUID) data).toString());

		else
			throw new IllegalArgumentException("Cannot create json element using " + data.getClass() + "!");
	}

}