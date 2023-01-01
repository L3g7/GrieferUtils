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

import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and injects the Mixin library.
 */
public class Injector implements IClassTransformer {

	private static final String MIXIN_CONFIG = "griefer_utils.mixins.json";
	private static final Map<String, Transformer> transformers = new HashMap<>();

	public Injector() throws ReflectiveOperationException, IOException {
		loadMixin();
		loadTransformers();
	}

	private void loadMixin() throws ReflectiveOperationException, IOException {
		File mixinLibrary = new File("libraries/org/spongepowered/mixin/0.7.11/mixin-0.7.11.jar");
		if (!mixinLibrary.exists()) {
			// Download library
			mixinLibrary.getParentFile().mkdirs();
			HttpsURLConnection c = (HttpsURLConnection) new URL("https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.7.11-SNAPSHOT/mixin-0.7.11-20180703.121122-1.jar").openConnection();
			c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
			Files.copy(c.getInputStream(), mixinLibrary.toPath());
		}

		// Add jar file to parent of LaunchClassLoader
		Field parent = Launch.classLoader.getClass().getDeclaredField("parent");
		parent.setAccessible(true);
		Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		addURL.setAccessible(true);
		addURL.invoke(parent.get(Launch.classLoader), mixinLibrary.toURI().toURL());
		addURL.invoke(Launch.classLoader, mixinLibrary.toURI().toURL());

		// Initialize Mixin
		MixinBootstrap.init();
		Mixins.addConfiguration(MIXIN_CONFIG);
		MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
	}

	private void loadTransformers() {
		for (ClassMeta meta : FileProvider.getClassesWithSuperClass(Transformer.class)) {
			Transformer transformer = Reflection.construct(meta.load());
			transformers.put(transformer.getTarget(), transformer);
		}
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
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