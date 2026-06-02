/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.injection;

import dev.l3g7.griefer_utils.core.api.reflection.Access;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.injection.InjectorBase;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.service.IMixinService;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Injector extends InjectorBase implements IClassTransformer {

	public Injector() throws ReflectiveOperationException {
		// Load MixinBootstrap using the system classloader
		Class<?> mixinBootstrap = Launch.classLoader.getClass().getClassLoader().loadClass("org.spongepowered.asm.launch.MixinBootstrap");
		mixinBootstrap.getDeclaredMethod("init").invoke(null);

		// Load MixinPlugin using the current classloader
		InjectorBase.class.getClassLoader().loadClass("dev.l3g7.griefer_utils.core.injection.MixinPlugin");
		InjectorBase.class.getClassLoader().loadClass("dev.l3g7.griefer_utils.core.injection.MixinPlugin$1");

		// Initialize injector
		InjectorBase.initialize(null, "LabyMod-3");

		// Finalize mixin initialization
		MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);

		if (!Reflection.exists("net.minecraftforge.common.ForgeHooks")) {
			// Account for transformers loading classes while GrieferUtils' mixin config is being initialised, causing the mixins not be applied // TODO what?
			Set<String> set = Reflection.get(MixinEnvironment.class, "excludeTransformers");
			set.add("net.labymod.addons.");
			MixinEnvironment.getDefaultEnvironment().setObfuscationContext("notch");
		} else {
			MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
		}

		// Wipe cached classes
		Class<?> mxInfoClass = Reflection.load("org.spongepowered.asm.mixin.transformer.MixinInfo");
		IMixinService classLoaderUtil0 = Reflection.get(mxInfoClass, "classLoaderUtil");
		Object classLoaderUtil = Reflection.get(classLoaderUtil0, "classLoaderUtil");
		Reflection.set(classLoaderUtil, "cachedClasses", new ConcurrentHashMap<>());

		Class<?> mixinEnv = Reflection.load("org.spongepowered.asm.mixin.MixinEnvironment");
		Field excludeTransformersField = Reflection.getField(mixinEnv, "excludeTransformers");
		Set<String> excludeTransformers = Reflection.get(mixinEnv, excludeTransformersField);

		MethodHandle setter = Access.getElevatedLookup().unreflectSetter(excludeTransformersField);
		try {
			setter.invoke(new HashSet<>(excludeTransformers) {
				@Override
				public boolean add(String s) {
					if (s.contains("griefer_utils"))
						return false;
					return super.add(s);
				}
			});
		} catch (Throwable t) {
			throw Util.elevate(t);
		}
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!shouldTransform(name, transformedName))
			return basicClass;

		return super.transform(name, transformedName, basicClass);
	}

}
