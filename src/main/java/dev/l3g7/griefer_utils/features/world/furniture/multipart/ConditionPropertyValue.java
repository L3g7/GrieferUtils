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

import com.google.common.base.Splitter;
import dev.l3g7.griefer_utils.features.world.furniture.util.Util;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConditionPropertyValue implements ICondition {

	private static final Splitter SPLITTER = Splitter.on('|').omitEmptyStrings();

	private final String key;
	private final String value;

	public ConditionPropertyValue(String keyIn, String valueIn) {
		this.key = keyIn;
		this.value = valueIn;
	}

	public Predicate<IBlockState> getPredicate(BlockState blockState) {
		IProperty<?> iproperty = (IProperty<?>)blockState.getProperties().stream()
				.filter((property) -> property.getName().equalsIgnoreCase(key))
				.findFirst().orElseThrow(() -> new RuntimeException(this + ": Definition: " + blockState + " has no property: " + key));

		String s = value;
		boolean invert = !s.isEmpty() && s.charAt(0) == '!';
		if (!s.isEmpty() && s.charAt(0) == '!')
			s = s.substring(1);

		List<String> list = SPLITTER.splitToList(s);
		if (list.isEmpty())
			throw new RuntimeException(this + ": has an empty value: " + value);

		Predicate<IBlockState> predicate = list.size() == 1
				? makePredicate(iproperty, s)
				: Predicates.or(list.stream()
					.map((value) -> makePredicate(iproperty, value))
					.collect(Collectors.toList()));

		return invert ? Predicates.not(predicate) : predicate;
	}

	private Predicate<IBlockState> makePredicate(IProperty<?> property, String valueIn) {
		Optional<?> optional = Util.parseProperty(property, valueIn);
		if (!optional.isPresent())
			throw new RuntimeException(this + ": has an unknown value: " + value);

		return state -> state != null && state.getValue(property).equals(optional.get());
	}
}