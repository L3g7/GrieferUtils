/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

package dev.l3g7.griefer_utils.util;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

import static com.github.lunatrius.schematica.proxy.ClientProxy.schematic;

public class SchematicaUtil {

	public static WorldClient getWorld() {
		return schematic;
	}

	public static ISchematic getSchematic() {
		return schematic.getSchematic();
	}

	public static MovingObjectPosition getMovingObjectPosition() {
		return ClientProxy.movingObjectPosition;
	}

	public static BlockPos getPosition() {
		return schematic.position;
	}

	public static boolean dontRender() {
		return schematic == null || !schematic.isRendering;
	}

	public static boolean shouldLayerBeRendered(int y) {
		return !schematic.isRenderingLayer || schematic.renderingLayer + schematic.position.field_177960_b == y;
	}

	public static void refresh() {
		RenderSchematic.INSTANCE.refresh();
	}

}
