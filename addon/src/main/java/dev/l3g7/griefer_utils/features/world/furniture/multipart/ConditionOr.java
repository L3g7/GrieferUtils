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

import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ConditionOr implements ICondition {

	private final Iterable<ICondition> conditions;

	public ConditionOr(Iterable<ICondition> conditionsIn) {
		this.conditions = conditionsIn;
	}

	@Override
	public Predicate<IBlockState> getPredicate(BlockState blockState) {
		return Predicates.or(StreamSupport.stream(conditions.spliterator(), false)
				.map(condition -> condition == null ? null : condition.getPredicate(blockState))
				.collect(Collectors.toList()));
	}
}
