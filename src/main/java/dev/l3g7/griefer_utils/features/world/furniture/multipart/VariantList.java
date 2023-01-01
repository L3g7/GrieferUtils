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

package dev.l3g7.griefer_utils.features.world.furniture.multipart;

import com.google.gson.*;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition.Variant;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class VariantList {
	private final List<Variant> variantList;

	public VariantList(List<Variant> variantListIn) {
		variantList = variantListIn;
	}

	public List<Variant> getVariantList() {
		return variantList;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;

		if (!(object instanceof VariantList))
			return false;

		return variantList.equals(((VariantList) object).variantList);
	}

	@Override
	public int hashCode() {
		return variantList.hashCode();
	}

	public static class Deserializer implements JsonDeserializer<VariantList> {

		public VariantList deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			ArrayList<Variant> list = new ArrayList<>();
			if (json.isJsonArray()) {
				JsonArray jsonarray = json.getAsJsonArray();
				if (jsonarray.size() == 0) {
					throw new JsonParseException("Empty variant array");
				}
				for (JsonElement jsonelement : jsonarray) {
					list.add((Variant)context.deserialize(jsonelement, (Type)((Object)Variant.class)));
				}
			} else {
				list.add((Variant)context.deserialize(json, (Type)((Object)Variant.class)));
			}
			return new VariantList(list);
		}

	}

}
