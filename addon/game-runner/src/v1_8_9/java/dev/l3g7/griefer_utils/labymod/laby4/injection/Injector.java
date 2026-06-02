/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.injection;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.injection.InjectorBase;
import net.labymod.api.BuildData;
import net.labymod.api.Laby;
import net.labymod.api.addon.LoadedAddon;
import net.labymod.api.addon.entrypoint.Entrypoint;
import net.labymod.api.loader.platform.PlatformClassTransformer;
import net.labymod.api.models.addon.annotation.AddonEntryPoint;
import net.labymod.api.models.addon.annotation.EarlyAddonTransformer;
import net.labymod.api.models.version.Version;
import net.labymod.api.util.version.SemanticVersion;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import java.util.Set;

@AddonEntryPoint
@EarlyAddonTransformer
public class Injector extends InjectorBase implements Entrypoint, IClassTransformer, PlatformClassTransformer {

	@Override
	public void initialize(Version version) {
		// Enable mixing into LabyMod's classes
		try {
			if (BuildData.version().isLowerThan(new SemanticVersion(4, 5))) {
				Reflection.set(Launch.classLoader, "parent", new TransformingParentClassLoader());

				Launch.classLoader.addClassLoaderExclusion("net.labymod.api.");
				Launch.classLoader.addClassLoaderExclusion("net.labymod.core.");
			} else {
				Set<String> transformerExceptions = Reflection.get(Launch.classLoader, "transformerExceptions");
				transformerExceptions.remove("net.labymod.api.");
				transformerExceptions.remove("net.labymod.core.");
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}

		// Load injector
		LoadedAddon addon = Laby.labyAPI().addonService().getAddon(getClass()).orElseThrow();
		InjectorBase.initialize(addon.info().getNamespace(), "LabyMod-4");
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!shouldTransform(name, transformedName))
			return basicClass;

		return super.transform(name, transformedName, basicClass);
	}

}
