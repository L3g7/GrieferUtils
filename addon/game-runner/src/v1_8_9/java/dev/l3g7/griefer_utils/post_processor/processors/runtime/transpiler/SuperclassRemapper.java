/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.runtime.transpiler;

import dev.l3g7.griefer_utils.core.api.mapping.Mapper;
import dev.l3g7.griefer_utils.post_processor.processors.RuntimePostProcessor;
import dev.l3g7.griefer_utils.post_processor.processors.runtime.transpiler.Java17to8Transpiler.BoundClassWriter;
import net.labymod.core.asm.LabyModCoreMod;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static dev.l3g7.griefer_utils.core.api.mapping.Mapping.SEARGE;
import static dev.l3g7.griefer_utils.core.api.mapping.Mapping.UNOBFUSCATED;
import static org.objectweb.asm.Type.*;

/**
 * Maps accesses to members of superclasses.
 */
public class SuperclassRemapper extends RuntimePostProcessor implements Opcodes {

	/**
	 * Set of already processed and skipped classes to avoid ClassCircularityErrors.
	 * TODO: shouldn't be required
	 */
	private final Set<String> classesWithoutMinecraft = new HashSet<>();

	private final boolean readClassBytes = !LabyModCoreMod.isForge();

	@Override
	public byte[] transform(String fileName, byte[] classBytes) {
		if (!fileName.startsWith("dev/l3g7/griefer_utils/"))
			return classBytes;

		ClassNode classNode = new ClassNode();
		ClassReader reader = new ClassReader(classBytes);
		reader.accept(classNode, 0);

		// Find minecraft superclasses
		List<String> checkedClasses = new ArrayList<>(Collections.singletonList(classNode.name));
		List<String> minecraftClasses = new ArrayList<>();
		if (classesWithoutMinecraft.contains(classNode.superName)) {
			classesWithoutMinecraft.addAll(checkedClasses);
			return classBytes;
		}

		var cb = getClass(classNode.superName);
		do {
			if (cb.name.startsWith("net/minecraft/"))
				minecraftClasses.add(cb.name);
			checkedClasses.add(cb.name);
			cb = getClass(cb.superName);
		} while (cb != null);

		if (minecraftClasses.isEmpty()) {
			classesWithoutMinecraft.addAll(checkedClasses);
			return classBytes;
		}

		// Map member accesses
		var it = classNode.methods.listIterator();
		while (it.hasNext()) {
			MethodNode method = it.next();
			for (AbstractInsnNode node : method.instructions) {
				// Map method invocations
				if (node instanceof MethodInsnNode methodInsn) {
					for (String minecraftClass : minecraftClasses) {
						String mappedName = Mapper.mapMethodName(minecraftClass, methodInsn.name, methodInsn.desc, UNOBFUSCATED, SEARGE);
						if (methodInsn.name.equals(mappedName))
							continue;

						methodInsn.name = mappedName;
						break;
					}
				}

				// Map field accesses
				else if (node instanceof FieldInsnNode field) {
					for (String minecraftClass : minecraftClasses) {
						String mappedName = Mapper.mapField(minecraftClass, field.name, UNOBFUSCATED, SEARGE);
						if (field.name.equals(mappedName))
							continue;

						field.name = mappedName;
						break;
					}
				}
			}

			// Map overrides
			for (String minecraftClass : minecraftClasses) {
				String mappedName = Mapper.mapMethodName(minecraftClass, method.name, method.desc, UNOBFUSCATED, SEARGE);
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
				break;
			}
		}

		ClassWriter writer = new BoundClassWriter();
		classNode.accept(writer);
		return writer.toByteArray();
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

	private ClassNode getClass(String name) {
		if (name == null)
			return null;

		if (readClassBytes) {
			byte[] bytes = getClassBytes(name);
			if (bytes == null)
				throw new RuntimeException(new NullPointerException("Couldn't find class bytes for " + name + "!"));

			bytes[7 /* major_version */] = 52 /* Java 1.8 */;
			ClassNode node = new ClassNode();
			new ClassReader(bytes).accept(node, 0);
			return node;
		}

		try {
			var clazz = Class.forName(name.replace('/', '.'));
			var c = new ClassNode();
			c.name = Type.getInternalName(clazz);
			c.superName = clazz.getSuperclass() == null ? null : Type.getInternalName(clazz.getSuperclass());
			return c;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] getClassBytes(String name) {
		try {
			byte[] classBytes = Launch.classLoader.getClassBytes(name);
			if (classBytes != null)
				return classBytes;

			String resourcePath = name.replace('.', '/').concat(".class");

			// Get stream
			InputStream in;
			if (Launch.class.getClassLoader() instanceof URLClassLoader ucl)
				in = ucl.getResourceAsStream(resourcePath);
			else {
				try (URLClassLoader ucl = new URLClassLoader(new URL[0], Launch.class.getClassLoader())) {
					in = ucl.getResourceAsStream(resourcePath);
				}
			}

			// Read stream
			try (in) {
				if (in == null)
					throw new IOException(new NullPointerException("InputStream is null"));

				@SuppressWarnings({"JavaExistingMethodCanBeUsed", "RedundantSuppression"})
				ByteArrayOutputStream output = new ByteArrayOutputStream();

				byte[] buffer = new byte[4096];
				int read;
				while ((read = in.read(buffer, 0, 4096)) >= 0)
					output.write(buffer, 0, read);

				return output.toByteArray();
			}
		} catch (IOException e) {
			return null;
		}
	}

}

