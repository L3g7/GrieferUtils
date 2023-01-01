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

package dev.l3g7.griefer_utils.features.world.furniture.blockshapes;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import static dev.l3g7.griefer_utils.util.misc.Constants.FURNITURE_RESOURCE_PACK_DIR;

public class BlockShapesLoader {
	private static final byte HIGHEST_SHAPES_VERSION = 13;
	public static final CompletableFuture<BlockShapes> SHAPES_FUTURE = new CompletableFuture<>();
	public static BlockShapes READ_SHAPES = new BlockShapes();

	static {
		new Thread(() -> {
			BlockShapes blockShapes = new BlockShapes();
			for (int i = 1; i <= HIGHEST_SHAPES_VERSION; ++i) {
				File file = new File(FURNITURE_RESOURCE_PACK_DIR, "assets/griefer_utils/shapes_v" + i + ".dat");
				try (DataInputStream dataInputStream = new DataInputStream(Files.newInputStream(file.toPath()))) {
					blockShapes = blockShapes.mergeShapes(BlockShapes.read(dataInputStream));
				} catch (IOException t) {
					t.printStackTrace();
				}
			}

			READ_SHAPES = blockShapes;
			SHAPES_FUTURE.complete(READ_SHAPES);
		}).start();
	}

}
