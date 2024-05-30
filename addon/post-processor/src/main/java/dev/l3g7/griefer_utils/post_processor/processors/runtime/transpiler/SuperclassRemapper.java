/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.runtime.transpiler;

import dev.l3g7.griefer_utils.api.mapping.Mapper;
import dev.l3g7.griefer_utils.post_processor.processors.RuntimePostProcessor;
import dev.l3g7.griefer_utils.post_processor.processors.runtime.transpiler.Java17to8Transpiler.BoundClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

import static dev.l3g7.griefer_utils.api.mapping.Mapping.SEARGE;
import static dev.l3g7.griefer_utils.api.mapping.Mapping.UNOBFUSCATED;
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
		List<Class<?>> minecraftClasses = new ArrayList<>();
		if (classesWithoutMinecraft.contains(classNode.superName)) {
			classesWithoutMinecraft.addAll(checkedClasses);
			return classBytes;
		}

		Class<?> clazz = forName(classNode.superName);
		do {
			if (clazz.getName().startsWith("net.minecraft."))
				minecraftClasses.add(clazz);
			checkedClasses.add(Type.getInternalName(clazz));
			clazz = clazz.getSuperclass();
		} while (clazz != null);

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
					for (Class<?> minecraftClass : minecraftClasses) {
						String mappedName = Mapper.mapMethodName(Type.getInternalName(minecraftClass), methodInsn.name, methodInsn.desc, UNOBFUSCATED, SEARGE);
						if (methodInsn.name.equals(mappedName))
							continue;

						methodInsn.name = mappedName;
						break;
					}
				}

				// Map field accesses
				else if (node instanceof FieldInsnNode field) {
					for (Class<?> minecraftClass : minecraftClasses) {
						String mappedName = Mapper.mapField(Type.getInternalName(minecraftClass), field.name, UNOBFUSCATED, SEARGE);
						if (field.name.equals(mappedName))
							continue;

						field.name = mappedName;
						break;
					}
				}
			}

			// Map overrides
			for (Class<?> minecraftClass : minecraftClasses) {
				String mappedName = Mapper.mapMethodName(Type.getInternalName(minecraftClass), method.name, method.desc, UNOBFUSCATED, SEARGE);
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

	private Class<?> forName(String name) {
		try {
			return Class.forName(name.replace('/', '.'));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private int getLoadOpcode(Type type) {
		return switch (type.getSort()) {
			case BOOLEAN, CHAR, BYTE, SHORT, INT -> ILOAD;
			case Type.LONG -> LLOAD;
			case Type.FLOAT -> FLOAD;
			case Type.DOUBLE -> DLOAD;
			case ARRAY, OBJECT -> ALOAD;
			default -> throw new IllegalArgumentException("Unrecognized type sort " + type.getSort());
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
			default -> throw new IllegalArgumentException("Unrecognized type sort " + type.getSort());
		};
	}

}
