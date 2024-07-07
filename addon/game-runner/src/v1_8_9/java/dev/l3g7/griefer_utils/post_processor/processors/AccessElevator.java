/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors;

import dev.l3g7.griefer_utils.post_processor.LatePostProcessor.Processor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Elevates the access of all classes, fields and methods to avoid having to generate synthetic accessors.
 */
public class AccessElevator extends Processor implements Opcodes {

	@Override
	public void process(ClassNode classNode) {
		// Don't elevate in Mixin classes
		if (classNode.invisibleAnnotations != null)
			for (AnnotationNode visibleAnnotation : classNode.invisibleAnnotations)
				if (visibleAnnotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;"))
					return;

		classNode.access = elevate(classNode.access);
		for (FieldNode field : classNode.fields)
			field.access = elevate(field.access);
		for (MethodNode method : classNode.methods)
			method.access = elevate(method.access);

		setModified();
	}

	private int elevate(int access) {
		access &= ~(ACC_PRIVATE | ACC_PROTECTED);
		access |= ACC_PUBLIC;
		return access;
	}

}
