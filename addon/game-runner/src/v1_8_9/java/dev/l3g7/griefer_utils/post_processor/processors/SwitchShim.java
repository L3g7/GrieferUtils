/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors;

import dev.l3g7.griefer_utils.post_processor.LatePostProcessor.Processor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * Replaces java/lang/MatchException with java/lang/RuntimeException.
 */
public class SwitchShim extends Processor implements Opcodes {

	@Override
	public void process(ClassNode classNode) {
		for (MethodNode method : classNode.methods) {
			// Patch MatchException
			for (AbstractInsnNode node : method.instructions) {
				if (node instanceof TypeInsnNode e) {
					if (e.getOpcode() == NEW && e.desc.equals("java/lang/MatchException")) {
						e.desc = "java/lang/RuntimeException";
						setModified();
					}
				} else if (node instanceof MethodInsnNode e) {
					if (e.getOpcode() == INVOKESPECIAL && e.owner.equals("java/lang/MatchException")) {
						e.owner = "java/lang/RuntimeException";
						setModified();
					}
				}
			}
		}
	}

}
