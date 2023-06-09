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

package dev.l3g7.griefer_utils.core;

import dev.l3g7.griefer_utils.core.mapping.Mapper;
import dev.l3g7.griefer_utils.core.misc.LibLoader;
import net.labymod.core.asm.LabyModCoreMod;
import net.minecraft.launchwrapper.IClassTransformer;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.IOException;

public class EarlyStart implements IClassTransformer {

	public EarlyStart() throws ReflectiveOperationException, IOException {
		if (!LabyModCoreMod.isForge())
			return;

		// Load mcp mappings for automatic name resolution in Reflection
		Mapper.loadMappings("1.8.9", "22");

		// Load and inject libraries
		LibLoader.loadLibraries();

		// Initialize Mixin
		MixinBootstrap.init();
		Mixins.addConfiguration("griefer_utils.mixins.json");
		MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) { return basicClass; }

}