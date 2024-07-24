/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors;

import dev.l3g7.griefer_utils.core.api.mapping.Mapper;
import dev.l3g7.griefer_utils.post_processor.LatePostProcessor.Processor;
import net.labymod.core.asm.LabyModCoreMod;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.mapping.Mapping.*;
import static org.objectweb.asm.Type.*;

/**
 * Maps accesses to members of superclasses.
 */
public class SuperclassRemapper extends Processor implements Opcodes {

	@Override
	public void process(ClassNode classNode) {
		if (classNode.name.startsWith("dev/l3g7/griefer_utils/core/api/"))
			return;

		// Find minecraft superclasses
		List<String> minecraftClasses = new ArrayList<>();
		boolean obfuscatedClasses;
		try {
			obfuscatedClasses = !LabyModCoreMod.isForge();
		} catch (NoClassDefFoundError error) {
			obfuscatedClasses = false; // Running SuperclassRemapper from BuildPostProcessor
		}

		var cb = getClass(classNode.superName);
		do {
			if (cb.name.startsWith("net/minecraft/"))
				minecraftClasses.add(cb.name);

			else if (!cb.name.contains("/")) {
				String mappedName = Mapper.mapClass(cb.name, OBFUSCATED, UNOBFUSCATED);
				if (mappedName.startsWith("net/minecraft/"))
					minecraftClasses.add(mappedName);
			}

			cb = getClass(cb.superName);
		} while (cb != null);

		if (minecraftClasses.isEmpty())
			return;

		// Map member accesses
		var it = classNode.methods.listIterator();
		while (it.hasNext()) {
			MethodNode method = it.next();
			for (AbstractInsnNode node : method.instructions) {
				// Map method invocations
				if (node instanceof MethodInsnNode methodInsn) {
					for (String minecraftClass : minecraftClasses) {
						String mappedName;
						if (obfuscatedClasses)
							mappedName = Mapper.mapMethodName(minecraftClass, methodInsn.name, deobfuscateDesc(methodInsn.desc), UNOBFUSCATED, OBFUSCATED);
						else
							mappedName = Mapper.mapMethodName(minecraftClass, methodInsn.name, methodInsn.desc, UNOBFUSCATED, SEARGE);
						if (methodInsn.name.equals(mappedName))
							continue;

						methodInsn.name = mappedName;
						setModified();
						break;
					}
				}

				// Map field accesses
				else if (node instanceof FieldInsnNode field) {
					for (String minecraftClass : minecraftClasses) {
						String mappedName = Mapper.mapField(minecraftClass, field.name, UNOBFUSCATED, obfuscatedClasses ? OBFUSCATED : SEARGE);
						if (field.name.equals(mappedName))
							continue;

						field.name = mappedName;
						setModified();
						break;
					}
				}
			}

			// Map overrides
			for (String minecraftClass : minecraftClasses) {
				String mappedName;
				if (obfuscatedClasses)
					mappedName = Mapper.mapMethodName(minecraftClass, method.name, deobfuscateDesc(method.desc), UNOBFUSCATED, OBFUSCATED);
				else
					mappedName = Mapper.mapMethodName(minecraftClass, method.name, method.desc, UNOBFUSCATED, SEARGE);
				if (method.name.equals(mappedName))
					continue;

				// Generate bridge
				MethodNode bridge = new MethodNode(method.access | ACC_SYNTHETIC, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0]));

				int varIdx = 0;
				bridge.instructions.add(new VarInsnNode(ALOAD, varIdx++));

				for (Type type : Type.getArgumentTypes(method.desc))
					bridge.instructions.add(new VarInsnNode(getLoadOpcode(type), varIdx++));

				method.name = mappedName;
				bridge.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, classNode.name, method.name, method.desc, false));
				bridge.instructions.add(new InsnNode(getReturnOpcode(Type.getReturnType(method.desc))));

				it.add(bridge);
				setModified();
				break;
			}
		}
	}

	private int getLoadOpcode(Type type) {
		return switch (type.getSort()) {
			case BOOLEAN, CHAR, BYTE, SHORT, INT -> ILOAD;
			case Type.LONG -> LLOAD;
			case Type.FLOAT -> FLOAD;
			case Type.DOUBLE -> DLOAD;
			case ARRAY, OBJECT -> ALOAD;
			default -> throw new IllegalArgumentException(String.valueOf(type.getSort()));
		};
	}

	private int getReturnOpcode(Type type) {
		return switch (type.getSort()) {
			case BOOLEAN, CHAR, BYTE, SHORT, INT -> IRETURN;
			case Type.LONG -> LRETURN;
			case Type.FLOAT -> FRETURN;
			case Type.DOUBLE -> DRETURN;
			case ARRAY, OBJECT -> ARETURN;
			case VOID -> RETURN;
			default -> throw new IllegalArgumentException(String.valueOf(type.getSort()));
		};
	}

	private String deobfuscateDesc(String desc) {
		var argumentTypes = Mapper.mapTypes(Type.getArgumentTypes(desc), OBFUSCATED, UNOBFUSCATED);
		var returnType = Mapper.mapType(Type.getReturnType(desc), OBFUSCATED, UNOBFUSCATED);
		return Type.getMethodDescriptor(returnType, argumentTypes);
	}

	private ClassNode getClass(String name) {
		if (name == null)
			return null;

		byte[] bytes;
		try {
			bytes = getClassBytes(name);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't find class bytes for " + name, e);
		}

		bytes[7 /* major_version */] = 52 /* Java 1.8 */;
		ClassNode node = new ClassNode();
		new ClassReader(bytes).accept(node, 0);
		return node;
	}

	private byte[] getClassBytes(String name) throws IOException { // TODO collect superclasses on build?
		if (Launch.classLoader != null) {
			byte[] classBytes = Launch.classLoader.getClassBytes(name);
			if (classBytes != null)
				return classBytes;
		}

		String resourcePath = name.replace('.', '/').concat(".class");

		if (Launch.class.getClassLoader() instanceof URLClassLoader ucl)
			return readClassBytes(name, ucl.getResourceAsStream(resourcePath));
		else {
			try (URLClassLoader ucl = new URLClassLoader(new URL[0], Launch.class.getClassLoader())) {
				return readClassBytes(name, ucl.getResourceAsStream(resourcePath));
			}
		}
	}

	private byte[] readClassBytes(String name, InputStream in) throws IOException {
		if (in == null) {
			// Try to get class bytes of mapped class
			String internalName = name.replace('.', '/');
			String mappedName = Mapper.mapClass(internalName, UNOBFUSCATED, OBFUSCATED);
			if (internalName.equals(mappedName))
				throw new IOException("Cannot find class bytes of unmapped class " + internalName);

			return getClassBytes(mappedName);
		}

		// Get stream
		try (in) {
			@SuppressWarnings({"JavaExistingMethodCanBeUsed", "RedundantSuppression"})
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			byte[] buffer = new byte[4096];
			int read;
			while ((read = in.read(buffer, 0, 4096)) >= 0)
				output.write(buffer, 0, read);

			return output.toByteArray();
		}
	}

}
