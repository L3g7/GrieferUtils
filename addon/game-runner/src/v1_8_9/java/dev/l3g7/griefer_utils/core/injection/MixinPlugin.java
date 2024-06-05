/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.injection;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MixinPlugin implements IMixinConfigPlugin {

	private final AtomicInteger mixinCount = new AtomicInteger();

	/**
	 * @return every class in GrieferUtils annotated with a valid {@link Mixin}.
	 */
	public List<String> getMixins() {
		// Remove mixin package (it can't be completely empty, otherwise it won't work with higher versions of mixin)
		Reflection.set(InjectorBase.mixinConfig.getConfig(), "mixinPackage", "dev/l3g7/");

		// Add mixin package again after every class is prepared
		Reflection.invoke(InjectorBase.mixinConfig.getConfig(), "addListener", createListener());

		List<String> classes = new ArrayList<>();

		// Find classes
		classFinder:
		for (String file : FileProvider.getFiles(f -> f.endsWith(".class"))) {
			ClassMeta meta = FileProvider.getClassMeta(file, false);
			if (meta == null || !meta.hasAnnotation(Mixin.class))
				continue;

			if (meta.hasAnnotation(ExclusiveTo.class)) {
				Version[] versions = meta.getAnnotation(ExclusiveTo.class).getValue("value", true);
				if (!Version.isCompatible(versions))
					continue;
			}

			ArrayList<Type> mixinTargets = meta.getAnnotation(Mixin.class).getValue("value", false);
			for (Type mixinTarget : mixinTargets) {
				// Only add mixin if the target exists, as some mixins target classes that might be missing (e.g. EmoteChat)
				if (!Reflection.exists(mixinTarget.getClassName())) {
					continue classFinder;
				}
			}
			classes.add(meta.toString().substring("dev/l3g7/".length()));

		}

		if (Reflection.exists("net.ccbluex.liquidbounce.injection.forge.mixins.render.MixinRendererLivingEntity"))
			classes.remove("griefer_utils/features/render/TrueSight$MixinRendererLivingEntity");
		if (Reflection.exists("net/minecraftforge/common/ForgeHooks"))
			classes.remove("griefer_utils/features/render/SkullEnchantmentFix$MixinFramebuffer");

		mixinCount.set(classes.size());
		return classes;
	}

	/**
	 * Creates a listener setting the mixin package after every class is prepared.
	 */
	private Object createListener() {
		Class<?> listenerClass = Reflection.load("org.spongepowered.asm.mixin.transformer.MixinConfig$IListener");
		AtomicInteger wtfIsGoingOnWithModules = mixinCount;
		return Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class[]{ listenerClass }, new InvocationHandler() {

			int currentMixin = 0;

			public Object invoke(Object proxy, Method method, Object[] args) {
				// Don't do anything in onInit
				if (!method.getName().equals("onPrepare"))
					return proxy;

				// Trigger after last mixin
				if (++currentMixin != wtfIsGoingOnWithModules.get())
					return null;

				// Ensure the given mixin package doesn't exist
				Reflection.set(InjectorBase.mixinConfig.getConfig(), "mixinPackage", UUID.randomUUID().toString());
				return null;
			}
		});
	}

	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return true; }
	public void onLoad(String mixinPackage) {}
	public String getRefMapperConfig() { return null; }
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

}