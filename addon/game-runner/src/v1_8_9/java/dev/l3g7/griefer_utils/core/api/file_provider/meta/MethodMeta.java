/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.file_provider.meta;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.util.ArrayUtil.flatmap;
import static dev.l3g7.griefer_utils.core.api.util.ArrayUtil.map;
import static dev.l3g7.griefer_utils.core.api.util.Util.elevate;

/**
 * Meta information of a method.
 */
public class MethodMeta implements IMeta {

	private final ClassMeta owner;
	private final String name;
	private final String desc;
	private final int modifiers;
	private final List<AnnotationMeta> annotations;

	public final MethodNode asmNode;
	private Method loadedMethod = null;

	/**
	 * Load the information from ASM's {@link MethodNode}.
	 */
	public MethodMeta(ClassMeta owner, MethodNode data) {
		this.owner = owner;
		this.name = data.name;
		this.desc = data.desc;
		this.modifiers = data.access;
		this.annotations = data.visibleAnnotations == null ? new ArrayList<>() : map(data.visibleAnnotations, AnnotationMeta::new);
		if (data.invisibleAnnotations != null)
			this.annotations.addAll(map(data.invisibleAnnotations, AnnotationMeta::new));

		this.asmNode = data;
	}

	/**
	 * Load the information from Reflection's {@link Method}.
	 */
	public MethodMeta(ClassMeta owner, Method method) {
		this.owner = owner;
		this.name = method.getName();
		this.desc = Type.getMethodDescriptor(method);
		this.modifiers = method.getModifiers();
		this.annotations = map(method.getAnnotations(), AnnotationMeta::new);

		this.asmNode = null;
		this.loadedMethod = method;
	}

	public ClassMeta owner() {
		return owner;
	}

	public String name() {
		return asmNode != null ? asmNode.name : name;
	}

	public String desc() {
		return asmNode != null ? asmNode.desc : desc;
	}

	public int modifiers() {
		return asmNode != null ? asmNode.access : modifiers;
	}

	@Override
	public List<AnnotationMeta> annotations() {
		if (asmNode != null) {
			List<AnnotationMeta> annotations = asmNode.visibleAnnotations == null ? new ArrayList<>() : map(asmNode.visibleAnnotations, AnnotationMeta::new);
			if (asmNode.invisibleAnnotations != null)
				annotations.addAll(map(asmNode.invisibleAnnotations, AnnotationMeta::new));

			return annotations;
		}
		return annotations;
	}

	/**
	 * Loads the method.
	 */
	public Method load() {
		if (loadedMethod != null)
			return loadedMethod;

		Class<?> ownerClass = owner.load();

		// Search for method in ownerClass
		for (Method method : flatmap(Method.class, ownerClass.getDeclaredMethods(), ownerClass.getMethods()))
			if (name.equals(method.getName()) && desc.equals(Type.getMethodDescriptor(method)))
				return loadedMethod = method;

		throw elevate(new NoSuchMethodException(), "Could not find method %s %s in %s!", name, desc, owner.name);
	}

	@Override
	public String toString() {
		return owner + "." + name + desc;
	}

}
