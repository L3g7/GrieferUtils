/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.injection;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.injection.InjectorBase;
import net.labymod.api.Laby;
import net.labymod.api.addon.LoadedAddon;
import net.minecraft.launchwrapper.Launch;

import java.util.Set;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@Bridge
@ExclusiveTo(LABY_4)
public class Injector implements InjectorBase {

	public Injector() {
		// Enable mixing into LabyMod's classes
		Set<String> transformerExceptions = Reflection.get(Launch.classLoader, "transformerExceptions");
		transformerExceptions.remove("net.labymod.api.");
		transformerExceptions.remove("net.labymod.core.");
		transformerExceptions.add("net.labymod.api.util.collection.map.Multimap");

		// Load injector
		LoadedAddon addon = Laby.labyAPI().addonService().getAddon(getClass()).orElseThrow();
		initMixin(addon.info().getNamespace(), null);
	}

}
