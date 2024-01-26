/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.api.file_provider;

import dev.l3g7.griefer_utils.api.bridges.Bridge;
import dev.l3g7.griefer_utils.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.api.file_provider.impl.JarFileProvider;
import dev.l3g7.griefer_utils.api.file_provider.impl.URLFileProvider;
import dev.l3g7.griefer_utils.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.api.file_provider.meta.MethodMeta;
import dev.l3g7.griefer_utils.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.api.util.IOUtil;
import dev.l3g7.griefer_utils.api.util.Util;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static org.objectweb.asm.ClassReader.SKIP_CODE;

/**
 * A class providing a list of all files in the addon.
 */
public abstract class FileProvider {

	/**
	 * Updates the cached files using refClass.
	 *
	 * @return the error if one occurred, null otherwise
	 */
	protected abstract Throwable update0(Class<?> refClass);

	protected static final Map<String, Supplier<InputStream>> fileCache = new HashMap<>();
	protected static final Set<String> exclusions = new HashSet<>();
	private static final Set<FileProvider> providers = new HashSet<>();

	private static final Map<String, ClassMeta> classMetaCache = new HashMap<>();
	private static final Map<Class<?>, Object> singletonInstances = new HashMap<>();

	/**
	 * Lazy loads all files if required and returns them.
	 */
	private static Map<String, Supplier<InputStream>> getFileCache() {
		if (providers.isEmpty()) {
			providers.add(JarFileProvider.INSTANCE);
			providers.add(URLFileProvider.INSTANCE);
			update(FileProvider.class);
		}

		return fileCache;
	}

	/**
	 * Triggers an update from all providers using the given class.
	 */
	public static void update(Class<?> refClass) {
		List<Throwable> errors = new ArrayList<>();

		// Initialize providers
		if (providers.isEmpty()) {
			providers.add(JarFileProvider.INSTANCE);
			providers.add(URLFileProvider.INSTANCE);
			update(FileProvider.class);
		}

		// Trigger all providers
		for (FileProvider provider : providers) {
			Throwable error = provider.update0(refClass);
			if (error != null)
				// Only throw errors if no provider was able to update
				errors.add(error);
		}

		if (errors.size() == providers.size()) {
			// If this point is reached, no provider was able to update -> throw errors
			errors.forEach(Throwable::printStackTrace);
			throw new RuntimeException("No available file provider could be found!");
		}
	}

	public static void exclude(String prefix) { // a/b.class
		exclusions.add(prefix);
		fileCache.entrySet().removeIf(e -> e.getKey().startsWith(prefix));
	}

	/**
	 * @return all known files.
	 */
	public static Collection<String> getFiles() {
		return getFileCache().keySet();
	}

	/**
	 * @return all files matching the filter.
	 */
	public static Collection<String> getFiles(Predicate<String> filter) {
		return getFiles().stream().filter(filter).collect(Collectors.toList());
	}

	/**
	 * @return the content of a file as an InputStream.
	 */
	public static InputStream getData(String file) {
		return getFileCache().get(file).get();
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
			new ClassReader(IOUtil.toByteArray(in)).accept(node, SKIP_CODE);

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
		return getClassesWithSuperClass(Type.getInternalName(superClass));
	}

	/**
	 * @return all classes with the specified super class.
	 */
	public static Collection<ClassMeta> getClassesWithSuperClass(String superClass) {
		List<ClassMeta> classes = new ArrayList<>();

		// Find classes
		for (String file : getFiles(f -> f.endsWith(".class"))) {
			ClassMeta meta = getClassMeta(file, false);
			if (meta != null && meta.hasSuperClass(superClass))
				classes.add(meta);
		}

		return classes;
	}

	/**
	 * @return all classes with the specified interface.
	 */
	public static Collection<ClassMeta> getClassesWithInterface(Class<?> checkedInterface) {
		return getClassesWithInterface(Type.getInternalName(checkedInterface));
	}

	/**
	 * @return all classes with the specified interface.
	 */
	public static Collection<ClassMeta> getClassesWithInterface(String checkedInterface) {
		List<ClassMeta> classes = new ArrayList<>();

		// Find classes
		for (String file : getFiles(f -> f.endsWith(".class"))) {
			ClassMeta meta = getClassMeta(file, false);
			if (meta != null && meta.hasInterface(checkedInterface))
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

	public static <I> I getBridge(Class<I> type) {
		if (!type.isAnnotationPresent(Bridged.class))
			throw new IllegalArgumentException(type + " is not bridged!");

		Collection<ClassMeta> bridges = getClassesWithInterface(type);

		if (bridges.isEmpty())
			throw new IllegalStateException("No fitting implementation for " + type + " found!");

		ClassMeta bridge = bridges.iterator().next();
		if (!bridge.hasAnnotation(Bridge.class))
			throw new IllegalArgumentException(bridge + " is not a bridge!");

		return getSingleton(bridge.load());
	}

}
