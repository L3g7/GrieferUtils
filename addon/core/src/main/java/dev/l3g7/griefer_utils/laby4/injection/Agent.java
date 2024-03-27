/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.injection;

import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.UUID;

public class Agent {

	public static void agentmain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
		ClassFileTransformer transformer = new ClassFileTransformer() {

			@Override
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
				if (!className.equals("net/minecraft/launchwrapper/LaunchClassLoader"))
					return null;

				ClassNode node = new ClassNode();
				ClassReader r = new ClassReader(classfileBuffer);
				r.accept(node, 0);

				for (MethodNode method : node.methods) {
					var it = method.instructions.iterator();
					while (it.hasNext())
						if (it.next() instanceof LdcInsnNode ldc && ("net.labymod.api.".equals(ldc.cst) || "net.labymod.core.".equals(ldc.cst)))
							ldc.cst = UUID.randomUUID().toString();
				}

				ClassWriter w = new ClassWriter(3);
				node.accept(w);
				return w.toByteArray();
			}

			@Override
			public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
				return transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
			}
		};
		inst.addTransformer(transformer, true);
		inst.retransformClasses(LaunchClassLoader.class);
		inst.removeTransformer(transformer);
		System.setProperty("griefer_utils_agent_flag", "");
	}

}
