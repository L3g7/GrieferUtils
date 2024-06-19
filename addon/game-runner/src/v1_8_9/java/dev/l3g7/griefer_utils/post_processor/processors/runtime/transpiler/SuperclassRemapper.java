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
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static dev.l3g7.griefer_utils.core.api.mapping.Mapping.SEARGE;
import static dev.l3g7.griefer_utils.core.api.mapping.Mapping.UNOBFUSCATED;
import static org.objectweb.asm.Type.*;

/**
 * Elevates the access of all classes, fields and methods to avoid having to generate synthetic accessors.
 */
public class SuperclassRemapper extends RuntimePostProcessor implements Opcodes {

	/**
	 * Set of already processed and skipped classes to avoid ClassCircularityErrors.
	 * TODO: shouldn't be required
	 */
	private final Set<String> classesWithoutMinecraft = new HashSet<>();

	@Override
	public byte[] transform(String fileName, byte[] classBytes) {
		if (!fileName.startsWith("dev/l3g7/griefer_utils/"))
			return classBytes;

		ClassNode classNode = new ClassNode();
		ClassReader reader = new ClassReader(classBytes);
		reader.accept(classNode, 0);

		// Find minecraft superclasses
		List<String> checkedClasses = new ArrayList<>(Collections.singletonList(classNode.name));
		List<ClassNode> minecraftClasses = new ArrayList<>();
		if (classesWithoutMinecraft.contains(classNode.superName)) {
			classesWithoutMinecraft.addAll(checkedClasses);
			return classBytes;
		}

		var cb = readClass(classNode.superName);
		do {
			if (cb.name.startsWith("net/minecraft/"))
				minecraftClasses.add(cb);
			checkedClasses.add(cb.name);
			cb = readClass(cb.superName);
		} while (cb != null);

		if (minecraftClasses.isEmpty()) {
			classesWithoutMinecraft.addAll(checkedClasses);
			return classBytes;
		}

		var it = classNode.methods.listIterator();
		while (it.hasNext()) {
			MethodNode method = it.next();
			for (AbstractInsnNode node : method.instructions) {
				// Map method invocations
				if (node instanceof MethodInsnNode methodInsn) {
					for (ClassNode minecraftClass : minecraftClasses) {
						String mappedName = Mapper.mapMethodName(minecraftClass.name, methodInsn.name, methodInsn.desc, UNOBFUSCATED, SEARGE);
						if (methodInsn.name.equals(mappedName))
							continue;

						methodInsn.name = mappedName;
						break;
					}
				}

				// Map field accesses
				else if (node instanceof FieldInsnNode field) {
					for (ClassNode minecraftClass : minecraftClasses) {
						String mappedName = Mapper.mapField(minecraftClass.name, field.name, UNOBFUSCATED, SEARGE);
						if (field.name.equals(mappedName))
							continue;

						field.name = mappedName;
						break;
					}
				}
			}

			// Map overrides
			for (ClassNode minecraftClass : minecraftClasses) {
				String mappedName = Mapper.mapMethodName(minecraftClass.name, method.name, method.desc, UNOBFUSCATED, SEARGE);
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
			default ->
				throw new IllegalArgumentException(new StringBuilder().append("Unrecognized type sort ").append(type.getSort()).toString());
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
			default ->
				throw new IllegalArgumentException(new StringBuilder().append("Unrecognized type sort ").append(type.getSort()).toString());
		};
	}

	private ClassNode readClass(String name) {
		if (name == null)
			return null;

		var cb = getClassBytes(name, name);
		if (cb == null) {
			System.out.println(new StringBuilder().append("FUCK ").append(name).append(" IS NULL :(;(;(").toString());
			return null;
		}
		cb[7 /* major_version */] = 52 /* Java 1.8 */;
		ClassReader r = new ClassReader(cb);
		ClassNode c = new ClassNode();
		r.accept(c, 0);
		return c;
	}

	public byte[] getClassBytes(String name, String transformedName) {
		byte[] classBytes = Launch.classLoader.getClassBytes(name);
		if (classBytes != null) {
			return classBytes;
		} else {
			URLClassLoader appClassLoader;
			if (Launch.class.getClassLoader() instanceof URLClassLoader) {
				appClassLoader = (URLClassLoader) Launch.class.getClassLoader();
			} else {
				appClassLoader = new URLClassLoader(new URL[0], Launch.class.getClassLoader());
			}

			InputStream classStream = null;

			Object var7;
			try {
				String resourcePath = transformedName.replace('.', '/').concat(".class");
				classStream = appClassLoader.getResourceAsStream(resourcePath);
				byte[] var13 = toByteArray(classStream);
				return var13;
			} catch (Exception var11) {
				var7 = null;
			} finally {
				if (classStream != null) {
					try {
						classStream.close();
					} catch (IOException ignored) {}
				}
			}

			return (byte[]) var7;
		}
	}

	public static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(32, in.available()));
		copy(in, out);
		return out.toByteArray();
	}

	public static long copy(InputStream from, OutputStream to) throws IOException {
		byte[] buf = new byte[8192];
		long total = 0L;

		while (true) {
			int r = from.read(buf);
			if (r == -1) {
				return total;
			}

			to.write(buf, 0, r);
			total += (long) r;
		}
	}
}
