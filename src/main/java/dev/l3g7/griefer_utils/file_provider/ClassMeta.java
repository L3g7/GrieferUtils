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

import dev.l3g7.griefer_utils.util.Util;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassReader.SKIP_CODE;

/**
 * A wrapper for the name of the classes super class.
 */
public class ClassMeta {

	private static final Map<String, ClassMeta> metaCache = new HashMap<>();
	private final List<String> methodAnnotations = new ArrayList<>();
	private final String superName;

	/**
	 * Loads the meta from the class data without loading it.
	 */
	public ClassMeta(byte[] data) {
		ClassNode node = new ClassNode();
		new ClassReader(data).accept(node, SKIP_CODE);
		superName = node.superName;

		for (MethodNode method : node.methods)
			for (AnnotationNode annotation : method.visibleAnnotations)
				methodAnnotations.add(annotation.desc);
	}

	/**
	 * Loads the meta from a class.
	 */
	public ClassMeta(Class<?> clazz) {
		Class<?> superClass = clazz.getSuperclass();
		this.superName = superClass == null ? null : Type.getInternalName(superClass);

		for (Method method : clazz.getMethods())
			for (Annotation annotation : method.getAnnotations())
				methodAnnotations.add(Type.getDescriptor(annotation.getClass()));
	}

	public String getSuperName() {
		return superName;
	}

	public List<String> getMethodAnnotations() {
		return methodAnnotations;
	}

	/**
	 * Returns true if the class has the specified super class.
	 */
	public boolean hasSuperClass(String superName) {
		ClassMeta meta = this;
		while (true) {
			if (superName.equals(meta.getSuperName()))
				return true;

			if (meta.getSuperName() == null)
				return false;

			meta = read(meta.getSuperName() + ".class");
		}
	}

	/**
	 * Returns true if the class has the specified annotation.
	 */
	public boolean hasMethodAnnotation(Class<? extends Annotation> annotation) {
		return getMethodAnnotations().contains(Type.getDescriptor(annotation));
	}

	/**
	 * Returns the class meta for a file.
	 */
	public static ClassMeta read(String file) {
		if (metaCache.containsKey(file))
			return metaCache.get(file);

		if (!FileProvider.getFiles().contains(file)) {
			ClassMeta meta = new ClassMeta(Reflection.load(file));
			metaCache.put(file, meta);
			return meta;
		}

		if (!file.endsWith(".class"))
			throw new IllegalArgumentException("Cannot load class meta of " + file);

		// Read ClassMeta
		try (InputStream in = FileProvider.getData(file)) {
			ClassMeta meta = new ClassMeta(IOUtils.toByteArray(in));
			metaCache.put(file, meta);
			return meta;
		} catch (IOException e) {
			throw Util.elevate(e, "Tried to read class meta of " + file);
		}
	}

}
