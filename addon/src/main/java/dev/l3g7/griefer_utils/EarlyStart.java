/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

import dev.l3g7.griefer_utils.core.mapping.Mapper;
import dev.l3g7.griefer_utils.core.misc.LibLoader;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.misc.ForgeModWarning;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.core.asm.LabyModTransformer;
import net.labymod.core.asm.mappings.Minecraft18MappingImplementation;
import net.labymod.core.asm.mappings.UnobfuscatedImplementation;
import net.minecraft.launchwrapper.Launch;

import java.io.IOException;

public class EarlyStart {

	public static void start() throws ReflectiveOperationException, IOException {

		// Load mcp mappings for automatic name resolution in Reflection
		Mapper.loadMappings("1.8.9", "22");

		// Load and inject libraries
		LibLoader.loadLibraries();

		// Sets LabyMod's mapping adapter
		// It's usually set in the MinecraftVisitor, but since Mixin changes the transformer order (i think),
		// transformers from LabyMod addons may be loaded before the MinecraftVisitor, causing the adapter to be null
		// and causing a crash if any addon tries to map something.
		Reflection.set(LabyModTransformer.class, LabyModCoreMod.isObfuscated() ? new Minecraft18MappingImplementation() : new UnobfuscatedImplementation(), "mappingImplementation");

		// Add Injector as transformer
		Launch.classLoader.registerTransformer("dev.l3g7.griefer_utils.injection.Injector");

		// Disable ForgeModWarning
		ForgeModWarning.loadedUsingLabyMod = true;
	}

}