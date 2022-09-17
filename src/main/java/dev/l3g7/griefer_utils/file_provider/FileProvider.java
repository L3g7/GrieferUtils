/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

import dev.l3g7.griefer_utils.file_provider.impl.JarFileProviderImpl;
import dev.l3g7.griefer_utils.file_provider.impl.URLFileProviderImpl;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import org.objectweb.asm.Type;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A class providing a list of all files in the addon.
 */
public class FileProvider {

	private static final Class<?>[] PROVIDERS = new Class[]{JarFileProviderImpl.class, URLFileProviderImpl.class};

	private static final Map<String, Class<?>> classes = new HashMap<>();
	private static final Map<Class<?>, Object> singletonInstances = new HashMap<>();
	private static FileProviderImpl impl = null;

	/**
	 * Lazy loads the implementation if required and returns it.
	 */
	private static FileProviderImpl getProvider() {
		if (impl != null)
			return impl;

		// Load implementation
		List<Throwable> errors = new ArrayList<>();
		for (Class<?> providerClass : PROVIDERS) {
			try {
				return impl = (FileProviderImpl) Reflection.construct(providerClass);
			} catch (Throwable throwable) {
				errors.add(throwable);
			}
		}

		// Only throw errors if no implementation could load
		errors.forEach(Throwable::printStackTrace);
		throw new RuntimeException("No available file provider could be found!");
	}

	/**
	 * Returns all files.
	 */
	public static Collection<String> getFiles() {
		return getProvider().getFiles();
	}

	/**
	 * Returns all files matching the filter.
	 */
	private static Collection<String> getFiles(Predicate<String> filter) {
		return getFiles().stream().filter(filter).collect(Collectors.toList());
	}

	/**
	 * Returns all files with a matching name.
	 */
	public static Collection<String> getFiles(String name) {
		return getFiles(f -> f.equals(name) || f.endsWith("/" + name));
	}

	/**
	 * Returns the content of a file as an InputStream.
	 */
	public static InputStream getData(String file) {
		return getProvider().getData(file);
	}

	/**
	 * Returns all classes with the specified super class.
	 */
	public static Collection<String> getClassesWithSuperClass(Class<?> superClass) {
		List<String> classes = new ArrayList<>();
		String internalName = Type.getInternalName(superClass);

		// Find classes
		for (String file : getFiles(f -> f.endsWith(".class")))
			if (ClassMeta.read(file).hasSuperClass(internalName))
				classes.add(file);

		return classes;
	}

	/**
	 * Returns all files with a matching name.
	 */
	public static Collection<String> getClassesWithAnnotatedMethods(Class<?> superClass, Class<? extends Annotation> annotation) {
		List<String> classes = new ArrayList<>();
		String internalName = Type.getInternalName(superClass);

		// Find classes
		for (String file : getFiles(f -> f.endsWith(".class")))
			if (ClassMeta.read(file).hasSuperClass(internalName))
				classes.add(file);

		return classes;
	}

	/**
	 * Loads the specified file as a class.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> loadClass(String file) {
		if (classes.containsKey(file))
			return (Class<T>) classes.get(file);

		Class<?> loadedClass = Reflection.load(file);
		classes.put(file, loadedClass);
		return (Class<T>) loadedClass;
	}

	/**
	 * Loads the specified class as a singleton.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getSingleton(Class<T> singleton) {
		if (singletonInstances.containsKey(singleton))
			return (T) singletonInstances.get(singleton);

		if (!singleton.isAnnotationPresent(Singleton.class))
			throw new IllegalArgumentException(singleton + " is not a singleton!");

		T loadedClass = Reflection.construct(singleton);
		singletonInstances.put(singleton, loadedClass);
		return loadedClass;
	}

}
