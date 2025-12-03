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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ListIterator;

/**
 * Replaces java/lang/MatchException with java/lang/RuntimeException and replaces {@link java.lang.runtime.SwitchBootstraps#typeSwitch(MethodHandles.Lookup, String, MethodType, Object...)}
 */
public class SwitchDowngrader extends Processor implements Opcodes {

	@Override
	public void process(ClassNode classNode) {
		for (MethodNode method : classNode.methods) {
			ListIterator<AbstractInsnNode> it = method.instructions.iterator();
			while (it.hasNext()) {
				AbstractInsnNode node = it.next();
				// Patch MatchException
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
				} else if (node instanceof InvokeDynamicInsnNode e) {
					if (e.bsm.getOwner().equals("java/lang/runtime/SwitchBootstraps") && e.bsm.getName().equals("typeSwitch")) {
						it.remove();

						// Generate call to typeSwitch
						assert e.bsmArgs.length <= Short.MAX_VALUE;
						it.add(new IntInsnNode(SIPUSH, e.bsmArgs.length));
						it.add(new TypeInsnNode(ANEWARRAY, "java/lang/Class"));

						for (int i = 0; i < e.bsmArgs.length; i++) {
							it.add(new InsnNode(DUP));
							it.add(new LdcInsnNode(i));
							it.add(new LdcInsnNode(e.bsmArgs[i]));
							it.add(new InsnNode(AASTORE));
						}

						it.add(new MethodInsnNode(INVOKESTATIC, SwitchDowngrader.class.getName().replace('.', '/'), "typeSwitch", "(Ljava/lang/Object;I[Ljava/lang/Class;)I"));
					}
				}
			}
		}
	}

	public static int typeSwitch(Object o, int n, Class<?>[] classes) {
		assert n == 0;

		if (o == null)
			return -1;

		for (int i = 0; i < classes.length; i++)
			if (classes[i].isInstance(o))
				return i;

		return -2;
	}

}
