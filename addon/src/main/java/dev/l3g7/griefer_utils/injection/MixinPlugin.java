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

package dev.l3g7.griefer_utils.injection;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import org.objectweb.asm.Type;
import org.spongepowered.asm.lib.tree.ClassNode;
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

public class MixinPlugin implements IMixinConfigPlugin {

	private int mixinCount;

	/**
	 * @return every class in GrieferUtils annotated with a valid {@link Mixin}.
	 */
	public List<String> getMixins() {
		// Remove mixin package
		Reflection.set(Injector.mixinConfig.getConfig(), "", "mixinPackage");

		// Add mixin package again after every class is prepared
		Reflection.invoke(Injector.mixinConfig.getConfig(), "addListener", createListener());

		List<String> classes = new ArrayList<>();

		// Find classes
		classFinder:
		for (String file : FileProvider.getFiles(f -> f.endsWith(".class"))) {
			ClassMeta meta = FileProvider.getClassMeta(file, false);
			if (meta == null || !meta.hasAnnotation(Mixin.class))
				continue;

			ArrayList<Type> mixinTargets = meta.getAnnotation(Mixin.class).getValue("value", false);
			for (Type mixinTarget : mixinTargets) {
				// Only add mixin if the target exists, as some mixins target classes that might be missing (e.g. EmoteChat)
				if (!Reflection.exists(mixinTarget.getClassName()))
					continue classFinder;
			}
			classes.add(meta.toString());

		}

		mixinCount = classes.size();
		return classes;
	}

	/**
	 * Creates a listener setting the mixin package after every class is prepared.
	 */
	private Object createListener() {
		Class<?> listenerClass = Reflection.load("org.spongepowered.asm.mixin.transformer.MixinConfig$IListener");
		return Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class[]{ listenerClass }, new InvocationHandler() {

			int currentMixin = 0;

			public Object invoke(Object proxy, Method method, Object[] args) {
				// Don't do anything in onInit
				if (!method.getName().equals("onPrepare"))
					return null;

				// Trigger after last mixin
				if (++currentMixin != mixinCount)
					return null;

				// Ensure the given mixin package doesn't exist
				Reflection.set(Injector.mixinConfig.getConfig(), UUID.randomUUID().toString(), "mixinPackage");
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