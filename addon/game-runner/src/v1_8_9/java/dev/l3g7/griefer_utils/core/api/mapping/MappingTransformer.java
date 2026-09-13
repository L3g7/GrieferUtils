/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.mapping;

import com.google.common.collect.ImmutableSet;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.pymdk.mapper.FastMapper;
import dev.pymdk.mapper.Mapping;
import dev.pymdk.mapper.impl.LowLevelMapper;
import dev.pymdk.mapper.impl.MappingEntries;
import dev.pymdk.mapper.impl.helpers.ClassScanner;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static dev.pymdk.mapper.Mapping.OBFUSCATED;
import static dev.pymdk.mapper.Mapping.UNOBFUSCATED;
import static org.objectweb.asm.ClassReader.*;

public class MappingTransformer implements IClassTransformer {

	private static final Set<String> FILTERED_PACKAGES = ImmutableSet.of("sun", "java", "javax");

	private final Mapping targetMapping;

	public MappingTransformer(Mapping targetMapping) {
		this.targetMapping = targetMapping;
	}

	private static final Set<String> processedClasses = new HashSet<>(512);

	@Override
	public byte[] transform(String name, String transformedName, byte[] classBytes) {
		byte[] copiedClassBytes = new byte[classBytes.length];
		System.arraycopy(classBytes, 0, copiedClassBytes, 0, classBytes.length);

		if (!transformedName.endsWith("FileProvider") && !LowLevelMapper.classes.unobfMap.containsKey(transformedName))
			registerClass(classBytes);

 		return FastMapper.mapClass(copiedClassBytes).getData();
	}

	private void registerClass(byte[] classBytes) {
		ClassNode node = new ClassNode();
		new ClassReader(classBytes).accept(node, SKIP_DEBUG | SKIP_CODE | SKIP_FRAMES);

		String className = node.name.replace('.', '/');
		if (!processedClasses.add(className))
			return;

		String unobfName = targetMapping != OBFUSCATED ? className : Mapper.mapClass(className, OBFUSCATED, UNOBFUSCATED);
		String obfName = targetMapping == OBFUSCATED ? className : Mapper.mapClass(className, UNOBFUSCATED, OBFUSCATED);
		MappingEntries.MappedClass mappedClass = new MappingEntries.MappedClass(obfName, unobfName);

		int rootPackageEnd = className.indexOf('/');
		if (rootPackageEnd == -1)
			return; // Obfuscated minecraft class, e.g. ave

		String rootPackage = className.substring(0, rootPackageEnd);

		if (FILTERED_PACKAGES.contains(rootPackage)) {
			LowLevelMapper.classes.unobfMap.put(unobfName, mappedClass);
			return;
		}

		List<String> baseClasses = new ArrayList<>(node.interfaces);
		baseClasses.add(node.superName);
		baseClasses.replaceAll(bc -> Mapper.mapClass(bc, targetMapping, UNOBFUSCATED));

		System.out.println("Loading refs of class" + node.name);
		loadAndRegister(baseClasses);
		mappedClass.setBaseClasses(baseClasses.toArray(new String[0]));
		LowLevelMapper.classes.unobfMap.put(unobfName, mappedClass);

		Set<String> flatClassRefs = ClassScanner.getClassReferences(classBytes).stream().map(s -> {
			if (!s.startsWith("["))
				return s;

			// Flatten arrays
			while (s.startsWith("["))
				s = s.substring(1);

			if (s.endsWith(";"))
				return s.substring(1, s.length() - 1);
			return null; // primitive, filter
		}).filter(Objects::nonNull).collect(Collectors.toSet());

		flatClassRefs.remove(node.name);
		baseClasses.forEach(flatClassRefs::remove);
		loadAndRegister(flatClassRefs);
		mappedClass.create();
	}

	private void loadAndRegister(Iterable<String> classNames) {
		for (String className : classNames) {
			if (className.length() == 1)
				continue; // flattened primitive array

			InputStream is = MappingTransformer.class.getResourceAsStream("/" + className + ".class");
			if (is == null)
				continue;

			try {
				registerClass(IOUtils.toByteArray(is));
			} catch (IOException e) {
				throw Util.elevate(e);
			}
		}
	}

}
