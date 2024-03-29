/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.injection;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.classobject.ClassObject;
import net.minecraft.launchwrapper.classobject.MutableClassObject;
import net.minecraft.launchwrapper.loader.BaseClassLoader;
import net.minecraft.launchwrapper.loader.ChildClassLoader;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;

import static java.lang.invoke.MethodType.methodType;

public class TransformingParentClassLoader extends ClassLoader {

	private final MethodHandles.Lookup LOOKUP;

	private final MethodHandle transformName;
	private final MethodHandle untransformName;
	private final MethodHandle findClassObject;
	private final MethodHandle runTransformers;
	private final MethodHandle addInvalidClass;

	private final MethodHandle classObjectHolderConstructor;
	private final MethodHandle getClassLoader;
	private final MethodHandle getClassObject;

	public TransformingParentClassLoader() throws ReflectiveOperationException {
		super(Launch.classLoader.getClass().getClassLoader());

		// Create elevated lookup
		LOOKUP = MethodHandles.lookup();
		Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Unsafe unsafe = (Unsafe) theUnsafe.get(null);
		unsafe.putInt(LOOKUP, 12 /* allowedModes */, -1 /* TRUSTED */);

		Class<?> classObjectHolder = LOOKUP.findClass("net.minecraft.launchwrapper.LaunchClassLoader$ClassObjectHolder");

		// Resolve methods
		transformName = findVirtual("transformName", methodType(String.class, String.class));
		untransformName = findVirtual("untransformName", methodType(String.class, String.class));
		findClassObject = findVirtual("findClassObject", methodType(classObjectHolder, String.class, BaseClassLoader.class));
		runTransformers = findVirtual("runTransformers", methodType(byte[].class, String.class, String.class, byte[].class));
		addInvalidClass = findVirtual("addInvalidClass", methodType(void.class, String.class));

		classObjectHolderConstructor = LOOKUP.findConstructor(classObjectHolder, methodType(void.class, BaseClassLoader.class, ClassObject.class));
		getClassLoader = LOOKUP.findVirtual(classObjectHolder, "getClassLoader", methodType(BaseClassLoader.class));
		getClassObject = LOOKUP.findVirtual(classObjectHolder, "getClassObject", methodType(ClassObject.class));
	}

	private MethodHandle findVirtual(String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
		MethodHandle handle = LOOKUP.findVirtual(Launch.classLoader.getClass(), name, type);
		return handle.bindTo(Launch.classLoader);
	}

	@Nullable
	@Override
	public URL getResource(String name) {
		return getParent().getResource(name);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (name.startsWith("net.labymod.api.") || name.startsWith("net.labymod.core.")) {
			try {
				return transform(name);
			} catch (Throwable e) {
				throw new ClassNotFoundException("Couldn't load " + name, e);
			}
		}

		return getParent().loadClass(name);
	}

	private Class<?> transform(String name) throws Throwable {
		String transformedName = (String) transformName.invoke(name);

		Class<?> cachedClass = Launch.classLoader.getCachedClass(transformedName);
		if (cachedClass != null)
			return cachedClass;

		Object holder;
		try {
			holder = findClassObject.invoke(name, (ChildClassLoader) null);
		} catch (ClassNotFoundException exception) {
			holder = classObjectHolderConstructor.invoke(Launch.classLoader, ClassObject.mutable(name));
		}

		ClassObject classObject = (ClassObject) getClassObject.invoke(holder);
		BaseClassLoader loader = (BaseClassLoader) getClassLoader.invoke(holder);

		String untransformedName = (String) untransformName.invoke(name);
		byte[] transformedClass = (byte[]) runTransformers.invoke(untransformedName, transformedName, classObject.getData());
		if (transformedClass == null) {
			addInvalidClass.invoke(name);
			throw new ClassNotFoundException(name);
		}

		if (classObject instanceof MutableClassObject)
			((MutableClassObject) classObject).setData(transformedClass);

		try {
			Class<?> cls = loader.defineClassObject(transformedName, transformedClass, 0, transformedClass.length, classObject.getCodeSource());
			Launch.classLoader.addClassToCache(name, cls);
			return cls;
		} catch (Exception e) {
			addInvalidClass.invoke(name);
			throw new ClassNotFoundException(name, e);
		}
	}

}
