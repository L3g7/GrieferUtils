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
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Replaces java/lang/MatchException with java/lang/RuntimeException.
 */
public class SwitchPolyfill extends RuntimePostProcessor implements Opcodes {

	@Override
	public byte[] transform(String fileName, byte[] classBytes) {
		if (!fileName.startsWith("dev/l3g7/griefer_utils/"))
			return classBytes;

		ClassNode classNode = new ClassNode();
		ClassReader reader = new ClassReader(classBytes);
		reader.accept(classNode, 0);
		AtomicBoolean modified = new AtomicBoolean(false);

		for (MethodNode method : classNode.methods) {
			// Patch MatchException
			method.instructions.iterator().forEachRemaining(node -> {
				if (node instanceof TypeInsnNode e) {
					if (e.getOpcode() == NEW && e.desc.equals("java/lang/MatchException")) {
						e.desc = "java/lang/RuntimeException";
						modified.set(true);
					}
				} else if (node instanceof MethodInsnNode e) {
					if (e.getOpcode() == INVOKESPECIAL && e.owner.equals("java/lang/MatchException")) {
						e.owner = "java/lang/RuntimeException";
						modified.set(true);
					}
				}
			});
		}

		// Write modified class
		if (!modified.get())
			return classBytes;

		ClassWriter writer = new BoundClassWriter();
		classNode.accept(writer);
		return writer.toByteArray();
	}

}
