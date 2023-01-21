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

package dev.l3g7.griefer_utils.features.world.furniture.abstraction;

import dev.l3g7.griefer_utils.features.world.furniture.multipart.Multipart;
import dev.l3g7.griefer_utils.features.world.furniture.multipart.VariantList;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;

import java.util.Map;
import java.util.Set;

public interface ModModelBlockDefinition {

	Map<String, ModelBlockDefinition.Variants> getMapVariants();

	boolean hasMultipartData();

	Multipart getMultipartData();

	void setMultipart(Multipart multipart);

	Set<VariantList> getMultipartVariants();

}
