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

import dev.l3g7.griefer_utils.features.world.furniture.block.ModBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.version_specific.VersionBlock;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import java.util.Map;
import java.util.Optional;

public class Util {

	public static ModBlock getCustomBlock(IBlockState state) {
		if (!(state.getBlock() instanceof VersionBlock))
			return null;

		return ((VersionBlock) state.getBlock()).getModBlock();
	}

	@SuppressWarnings("unchecked")
	public static <V extends Comparable<V>> IBlockState withProperties(IBlockState state, Object... args) {
		for (int i = 0; i < args.length;)
			state = state.withProperty((IProperty<V>) args[i++], (V) args[i++]);

		return state;
	}

	@SuppressWarnings("unchecked")
	public static <V extends Comparable<V>> IBlockState withSameValueProperties(IBlockState state, V value, IProperty<V>... properties) {
		for (IProperty<V> property : properties)
			state = state.withProperty(property, value);

		return state;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> Optional<T> parseProperty(IProperty<T> property, String value) {
		if (property instanceof PropertyBool)
			return (Optional<T>) Optional.of(Boolean.parseBoolean(value));

		if (property instanceof PropertyInteger) {
			try {
				Integer i = Integer.valueOf(value);
				return property.getAllowedValues().contains(i) ? (Optional<T>) Optional.of(i) : Optional.empty();
			} catch (NumberFormatException ignored) {
				return Optional.empty();
			}
		}

		// PropertyEnum
		Map<String, T> nameToValue = Reflection.get(property, "nameToValue");
		return Optional.ofNullable(nameToValue.get(value));
	}

	public static EnumFacing getHorizontal(int ordinal) {
		return EnumFacing.values()[ordinal + 2];
	}

}
