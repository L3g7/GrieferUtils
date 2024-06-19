/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.runtime.transpiler;

import dev.l3g7.griefer_utils.post_processor.processors.RuntimePostProcessor;
import dev.l3g7.griefer_utils.post_processor.processors.runtime.transpiler.Java17to8Transpiler.BoundClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Elevates the access of all classes, fields and methods to avoid having to generate synthetic accessors.
 */
public class AccessElevator extends RuntimePostProcessor implements Opcodes {

	@Override
	public byte[] transform(String fileName, byte[] classBytes) {
		if (!fileName.startsWith("dev/l3g7/griefer_utils/"))
			return classBytes;

		ClassNode classNode = new ClassNode();
		ClassReader reader = new ClassReader(classBytes);
		reader.accept(classNode, 0);

		// Don't elevate in Mixin classes
		if (classNode.invisibleAnnotations != null)
			for (AnnotationNode visibleAnnotation : classNode.invisibleAnnotations)
				if (visibleAnnotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;"))
					return classBytes;

		classNode.access = elevate(classNode.access);
		for (FieldNode field : classNode.fields)
			field.access = elevate(field.access);
		for (MethodNode method : classNode.methods)
			method.access = elevate(method.access);

		ClassWriter writer = new BoundClassWriter();
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private int elevate(int access) {
		access &= ~(ACC_PRIVATE | ACC_PROTECTED);
		access |= ACC_PUBLIC;
		return access;
	}

}
