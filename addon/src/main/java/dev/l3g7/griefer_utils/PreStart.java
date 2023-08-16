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

package dev.l3g7.griefer_utils;

import dev.l3g7.griefer_utils.core.auto_update.AutoUpdater;
import net.minecraft.launchwrapper.IClassTransformer;

/**
 * A wrapper class triggering the {@link AutoUpdater} and then loading {@link EarlyStart}.
 * This ensures {@link EarlyStart} has not been loaded before an update occurs.
 */
public class PreStart implements IClassTransformer {

	public PreStart() throws Exception {
		AutoUpdater.update();
		EarlyStart.start();
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) { return basicClass; }

}
