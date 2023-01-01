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

package dev.l3g7.griefer_utils.file_provider;

import dev.l3g7.griefer_utils.file_provider.impl.JarFileProvider;
import dev.l3g7.griefer_utils.file_provider.impl.URLFileProvider;
import dev.l3g7.griefer_utils.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.file_provider.meta.MethodMeta;
import dev.l3g7.griefer_utils.util.Util;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.util.Util.elevate;
import static dev.l3g7.griefer_utils.util.reflection.Reflection.c;
import static org.objectweb.asm.ClassReader.SKIP_CODE;

/**
 * A class providing a list of all files in the addon.
 */
public abstract class FileProvider {

	/**
	 * Updates the known files using refClass.
	 * @return the error if one occurred, null otherwise
	 */
	protected abstract Throwable update0(Class<?> refClass);

	/**
	 * @return a list of all known files.
	 */
	protected abstract Collection<String> getFiles0();

	/**
	 * @return an InputStream containing the given file's contents
	 */
	protected abstract InputStream getData0(String file);



	private static final Map<String, ClassMeta> classMetaCache = new HashMap<>(); // file -> class meta
	private static final Map<Class<?>, Object> singletonInstances = new HashMap<>();
	private static FileProvider provider = null;

	/**
	 * Lazy loads the implementation if required and returns it.
	 */
	private static FileProvider getProvider() {
		if (provider == null) {
			List<Throwable> errors = new ArrayList<>();

			// Test possible providers
			for (FileProvider possibleProvider : new FileProvider[]{JarFileProvider.INSTANCE, URLFileProvider.INSTANCE}) {
				Throwable error = possibleProvider.update0(FileProvider.class);
				if (error == null)
					return provider = possibleProvider;

				// Only throw errors if no implementation could load
				errors.add(error);
			}

			// If this point is reached, no implementation could be loaded -> throw errors
			errors.forEach(Throwable::printStackTrace);
			throw new RuntimeException("No available file provider could be found!");
		}

		return provider;
	}

	/**
	 * Updates the current provider using the given class.
	 * Currently unused, makes possible extensions of GrieferUtils easier.
	 */
	@SuppressWarnings("unused")
	public static void update(Class<?> refClass) {
		Throwable error = getProvider().update0(refClass);
		if (error != null)
			throw elevate(error);
	}



	/**
	 * @return all known files.
	 */
	public static Collection<String> getFiles() {
		return getProvider().getFiles0();
	}

	/**
	 * @return all files matching the filter.
	 */
	private static Collection<String> getFiles(Predicate<String> filter) {
		return getFiles().stream().filter(filter).collect(Collectors.toList());
	}

	/**
	 * @return the content of a file as an InputStream.
	 */
	public static InputStream getData(String file) {
		return getProvider().getData0(file);
	}

	/**
	 * @return the class meta for a class based on its description.
	 */
	public static ClassMeta getClassMetaByDesc(String desc) {
		return getClassMeta(Type.getType(desc).getInternalName() + ".class", true);
	}

	/**
	 * @param loadUnknownFiles whether files not known for the provider should be loaded using reflection.
	 * @return the class meta for a file.
	 */
	public static ClassMeta getClassMeta(String file, boolean loadUnknownFiles) {
		if (classMetaCache.containsKey(file))
			return classMetaCache.get(file);

		// Check if file is known
		if (!getFiles().contains(file)) {
			if (!loadUnknownFiles)
				return null;
			// Load ClassMeta using Reflection
			ClassMeta meta = new ClassMeta(Reflection.load(file));
			classMetaCache.put(file, meta);
			return meta;
		}

		if (!file.endsWith(".class"))
			throw new IllegalArgumentException("Cannot load class meta of " + file);

		// Load ClassMeta using ASM
		try (InputStream in = getData(file)) {
			ClassNode node = new ClassNode();
			new ClassReader(IOUtils.toByteArray(in)).accept(node, SKIP_CODE);

			ClassMeta meta = new ClassMeta(node);
			classMetaCache.put(file, meta);
			return meta;
		} catch (IOException e) {
			throw Util.elevate(e, "Tried to read class meta of " + file);
		}
	}

	/**
	 * @return all classes with the specified super class.
	 */
	public static Collection<ClassMeta> getClassesWithSuperClass(Class<?> superClass) {
		List<ClassMeta> classes = new ArrayList<>();
		String internalName = Type.getInternalName(superClass);

		// Find classes
		for (String file : getFiles(f -> f.endsWith(".class"))) {
			ClassMeta meta = getClassMeta(file, false);
			if (meta != null && meta.hasSuperClass(internalName))
				classes.add(meta);
		}

		return classes;
	}

	/**
	 * @return all methods with the given annotation present.
	 */
	public static Collection<MethodMeta> getAnnotatedMethods(Class<? extends Annotation> annotation) {
		List<MethodMeta> methods = new ArrayList<>();
		String annotationName = Type.getDescriptor(annotation);

		// Find classes
		for (String file : getFiles(f -> f.endsWith(".class"))) {
			ClassMeta meta = getClassMeta(file, false);
			if (meta == null)
				continue;

			for (MethodMeta method : meta.methods)
				if (method.hasAnnotation(annotationName))
					methods.add(method);
		}

		return methods;
	}

	/**
	 * Loads the specified class as a singleton.
	 */
	public static <T> T getSingleton(Class<T> singleton) {
		if (singletonInstances.containsKey(singleton))
			return c(singletonInstances.get(singleton));

		if (!singleton.isAnnotationPresent(Singleton.class))
			throw new IllegalArgumentException(singleton + " is not a singleton!");

		T loadedClass = Reflection.construct(singleton);
		singletonInstances.put(singleton, loadedClass);
		return loadedClass;
	}

}
