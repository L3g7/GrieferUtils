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

package dev.l3g7.griefer_utils.injection;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.mapping.Mapping;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.service.IMixinService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Injector implements IClassTransformer {

	static Config mixinConfig;
	private static final Map<String, Transformer> transformers = new HashMap<>();

	public Injector() {
		// Initialize Mixin
		MixinBootstrap.init();

		// Account for transformers loading classes while grieferutils' mixin config is being initialised, causing the mixins not be applied
		Set<String> set = Reflection.get(MixinEnvironment.class, "excludeTransformers");
		set.add("net.labymod.addons.");

		mixinConfig = Config.create("griefer_utils.mixins.json");
		Reflection.invoke(Mixins.class, "registerConfiguration", mixinConfig);
		MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);

		if (!Reflection.exists("net.minecraftforge.common.ForgeHooks")) {
			MixinEnvironment.getDefaultEnvironment().setObfuscationContext("notch");
			Reflection.setMappingTarget(Mapping.OBFUSCATED);
		}

		try {
			Class<?> mxInfoClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo");
			IMixinService classLoaderUtil0 = Reflection.get(mxInfoClass, "classLoaderUtil");
			Object classLoaderUtil = Reflection.get(classLoaderUtil0, "classLoaderUtil");
			Reflection.set(classLoaderUtil, new ConcurrentHashMap<>(), "cachedClasses");
		} catch (ReflectiveOperationException ignored) {}

		// Load transformers
		for (ClassMeta meta : FileProvider.getClassesWithSuperClass(Transformer.class)) {
			Transformer transformer = Reflection.construct(meta.load());
			transformers.put(transformer.getTarget(), transformer);
		}
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (name.startsWith("com.github.lunatrius.schematica"))
			Constants.SCHEMATICA = true;

		Transformer transformer = transformers.get(transformedName);
		if (transformer != null) {
			ClassNode classNode = new ClassNode();
			ClassReader reader = new ClassReader(basicClass);
			reader.accept(classNode, 0);

			transformer.transform(classNode);

			ClassWriter writer = new ClassWriter(3);
			classNode.accept(writer);
			return writer.toByteArray();
		}

		return basicClass;
	}

}
