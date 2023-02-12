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

package dev.l3g7.griefer_utils.core.file_provider.meta;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.core.util.ArrayUtil.map;
import static dev.l3g7.griefer_utils.core.reflection.Reflection.c;

/**
 * Meta information of a class.
 */
public class ClassMeta implements IMeta {

	public final String name;
	public final String superName;
	public final int modifiers;
	public final String signature;
	public final List<MethodMeta> methods;
	public final List<AnnotationMeta> annotations;

	public final ClassNode asmNode;
	private Class<?> loadedClass = null;

	/**
	 * Load the information from ASM's {@link ClassNode}.
	 */
	public ClassMeta(ClassNode node) {
		this.name = node.name;
		this.superName = node.superName;
		this.modifiers = node.access;
		this.signature = node.signature;
		this.methods = map(node.methods, m -> new MethodMeta(this, m));
		this.annotations = node.visibleAnnotations == null ? Collections.emptyList() : map(node.visibleAnnotations, AnnotationMeta::new);

		this.asmNode = node;
	}

	/**
	 * Load the information from Reflection's {@link Class}.
	 */
	public ClassMeta(Class<?> clazz) {
		Class<?> superClass = clazz.getSuperclass();

		this.name = Type.getInternalName(clazz);
		this.superName = superClass == null ? null : Type.getInternalName(superClass);
		this.modifiers = clazz.getModifiers();
		this.signature = null;
		this.methods = map(clazz.getDeclaredMethods(), m -> new MethodMeta(this, m));
		this.annotations = map(clazz.getAnnotations(), AnnotationMeta::new);

		this.asmNode = null;
		this.loadedClass = clazz;
	}

	/**
	 * @return true if the class has the specified super class.
	 */
	public boolean hasSuperClass(String superName) {
		ClassMeta meta = this;
		while (true) {
			if (superName.equals(meta.superName))
				return true;

			if (meta.superName == null)
				return false;

			meta = FileProvider.getClassMeta(meta.superName + ".class", false);
			if (meta == null)
				return false;
		}
	}

	/**
	 * Loads the class.
	 */
	public <T> Class<T> load() {
		if (loadedClass == null)
			loadedClass = Reflection.load(name);

		return c(loadedClass);
	}

	@Override
	public List<AnnotationMeta> annotations() {
		return annotations;
	}

	@Override
	public int modifiers() {
		return modifiers;
	}

	@Override
	public String toString() {
		return name;
	}

}
