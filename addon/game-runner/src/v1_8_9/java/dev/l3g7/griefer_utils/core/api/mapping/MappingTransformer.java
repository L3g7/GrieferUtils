/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.mapping;

import dev.pymdk.mapper.FastMapper;
import dev.pymdk.mapper.Mapping;
import dev.pymdk.mapper.impl.LowLevelMapper;
import dev.pymdk.mapper.impl.MappingEntries;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.pymdk.mapper.Mapping.OBFUSCATED;
import static dev.pymdk.mapper.Mapping.UNOBFUSCATED;
import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;

public class MappingTransformer implements IClassTransformer {

	private final Mapping targetMapping;

	public MappingTransformer(Mapping targetMapping) {
		this.targetMapping = targetMapping;
	}

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
	 * @return The class' unobfuscated name.
	 */
	private String registerClassesRecursively(Class<?> clazz) {
		String className = clazz.getName().replace('.', '/');
		String unobfName = targetMapping != OBFUSCATED ? className : Mapper.mapClass(className, OBFUSCATED, UNOBFUSCATED);
		if (LowLevelMapper.classes.unobfMap.containsKey(unobfName))
			return unobfName; // Already registered

		String obfName = targetMapping == OBFUSCATED ? className : Mapper.mapClass(className, UNOBFUSCATED, OBFUSCATED);
		MappingEntries.MappedClass mappedClass = new MappingEntries.MappedClass(obfName, unobfName);

		// Register interfaces & super class
		List<Class<?>> baseClasses = new ArrayList<>(Arrays.asList(clazz.getInterfaces()));
		if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class)
			baseClasses.add(clazz.getSuperclass());

		String[] baseClassNames = baseClasses.stream()
			.map(this::registerClassesRecursively)
			.toArray(String[]::new);

		mappedClass.setBaseClasses(baseClassNames);

		// Register this class
		LowLevelMapper.classes.unobfMap.put(unobfName, mappedClass);
		mappedClass.create();
		return unobfName;
	}

}
