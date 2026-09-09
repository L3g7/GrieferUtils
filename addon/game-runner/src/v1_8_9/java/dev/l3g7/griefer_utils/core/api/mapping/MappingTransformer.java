/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.mapping;

import dev.pymdk.mapper.FastMapper;
import dev.pymdk.mapper.impl.LowLevelMapper;
import dev.pymdk.mapper.impl.MappingEntries;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;

public class MappingTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] classBytes) {
		byte[] copiedClassBytes = new byte[classBytes.length];
		System.arraycopy(classBytes, 0, copiedClassBytes, 0, classBytes.length);

		// Register base classes
		ClassNode node = new ClassNode();
		new ClassReader(classBytes).accept(node, SKIP_CODE | SKIP_FRAMES);

		if (LowLevelMapper.classes.unobfMap.containsKey(node.name))
			return FastMapper.mapClass(copiedClassBytes).getData(); // Already registered

		MappingEntries.MappedClass mappedClass = new MappingEntries.MappedClass(node.name, node.name);
		node.interfaces.add(node.superName);

		for (String anInterface : node.interfaces) {
			try {
				Class<?> clazz = Class.forName(anInterface.replace('/', '.'), false, Launch.classLoader);
				registerClassesRecursively(clazz);
			} catch (ClassNotFoundException e) {
				// ignore, probably not mapped
			}
		}

		mappedClass.setBaseClasses(node.interfaces.toArray(new String[0]));
		LowLevelMapper.classes.unobfMap.put(node.name, mappedClass);
		mappedClass.create();

		return FastMapper.mapClass(copiedClassBytes).getData();
	}

	/**
	 * Registers the given class in LowLevelMapper's mappings.
	 */
	private static void registerClassesRecursively(Class<?> clazz) {
		String className = clazz.getName().replace('.', '/');
		if (LowLevelMapper.classes.unobfMap.containsKey(className))
			return; // Already registered

		MappingEntries.MappedClass mappedClass = new MappingEntries.MappedClass(className, className);

		List<Class<?>> baseClasses = new ArrayList<>(Arrays.asList(clazz.getInterfaces()));
		if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class)
			baseClasses.add(clazz.getSuperclass());

		for (Class<?> baseClass : baseClasses)
			registerClassesRecursively(baseClass);

		mappedClass.setBaseClasses(baseClasses.stream()
			.map(c -> c.getName().replace('.', '/'))
			.toArray(String[]::new));

		LowLevelMapper.classes.unobfMap.put(className, mappedClass);
		mappedClass.create();
	}

}
