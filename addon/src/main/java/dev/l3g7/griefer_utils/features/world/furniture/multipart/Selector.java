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

package dev.l3g7.griefer_utils.features.world.furniture.multipart;

import com.google.gson.*;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Selector {
	private final ICondition condition;
	private final VariantList variantList;

	public Selector(ICondition conditionIn, VariantList variantListIn) {
		if (conditionIn == null)
			throw new IllegalArgumentException("Missing condition for selector");

		if (variantListIn == null)
			throw new IllegalArgumentException("Missing variant for selector");

		condition = conditionIn;
		variantList = variantListIn;
	}

	public VariantList getVariantList() {
		return variantList;
	}

	public Predicate<IBlockState> getPredicate(BlockState state) {
		return condition.getPredicate(state);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;

		if (!(object instanceof Selector))
			return false;

		Selector other = (Selector) object;
		return condition.equals(other.condition) && variantList.equals(other.variantList);
	}

	@Override
	public int hashCode() {
		return 31 * condition.hashCode() + variantList.hashCode();
	}

	public static class Deserializer implements JsonDeserializer<Selector> {
		private static final Function<JsonElement, ICondition> FUNCTION_OR_AND = (json -> (json == null) ? null : getOrAndCondition(json.getAsJsonObject()));
		private static final Function<Map.Entry<String, JsonElement>, ICondition> FUNCTION_PROPERTY_VALUE = (map -> (map == null) ? null : makePropertyValue(map));

		public Selector deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
			JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
			return new Selector(getWhenCondition(jsonobject), p_deserialize_3_.deserialize(jsonobject.get("apply"), VariantList.class));
		}

		private ICondition getWhenCondition(JsonObject json) {
			return json.has("when") ? getOrAndCondition(JsonUtils.getJsonObject(json, "when")) : ICondition.TRUE;
		}

		static ICondition getOrAndCondition(JsonObject json) {
			Set<Map.Entry<String, JsonElement>> set = json.entrySet();
			if (set.isEmpty())
				throw new JsonParseException("No elements found in selector");

			if (set.size() != 1)
				return new ConditionAnd(
						set.stream()
						.map(FUNCTION_PROPERTY_VALUE)
						.collect(Collectors.toList())
				);

			if (json.has("OR"))
				return new ConditionOr(StreamSupport.stream(JsonUtils.getJsonArray(json, "OR").spliterator(), false).map(FUNCTION_OR_AND).collect(Collectors.toList()));

			return json.has("AND")
					? new ConditionAnd(
						StreamSupport.stream(JsonUtils.getJsonArray(json, "AND").spliterator(), false)
							.map(FUNCTION_OR_AND)
							.collect(Collectors.toList())
					  )
					: makePropertyValue(set.iterator().next()
			);
		}

		private static ConditionPropertyValue makePropertyValue(Map.Entry<String, JsonElement> entry) {
			return new ConditionPropertyValue(entry.getKey(), entry.getValue().getAsString());
		}

	}

}