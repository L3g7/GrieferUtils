/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.build;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import static org.objectweb.asm.Type.ARRAY;

/**
 * Converts records to classes.
 */
public class RecordConverter implements Opcodes {

	private static final Converter[] CONVERTERS = new Converter[] {
		new Converter("Boolean", 'Z'),
		new Converter("Character", 'C'),
		new Converter("Byte", 'B'),
		new Converter("Short", 'S'),
		new Converter("Integer", 'I'),
		new Converter("Float", 'F'),
		new Converter("Long", 'J'),
		new Converter("Double", 'D')
	};

	public static boolean process(ClassNode classNode) {
		// Only process records
		if ((classNode.access & Opcodes.ACC_RECORD) == 0)
			return false;

		classNode.access &= ~Opcodes.ACC_RECORD;
		if (classNode.superName.equals("java/lang/Record"))
			classNode.superName = "java/lang/Object";

		Map<String, MethodNode> methodNodes = new HashMap<>();
		for (MethodNode method : classNode.methods)
			methodNodes.put(method.name + method.desc, method);

		// Fix default methods
		MethodNode toString = methodNodes.get("toString()Ljava/lang/String;");
		if (toString.instructions.size() == 6 && toString.instructions.get(3) instanceof InvokeDynamicInsnNode d && d.bsm.getOwner().equals("java/lang/runtime/ObjectMethods"))
			toString.instructions = generateToString(classNode.name, classNode.recordComponents);

		MethodNode hashCode = methodNodes.get("hashCode()I");
		if (hashCode.instructions.size() == 6 && hashCode.instructions.get(3) instanceof InvokeDynamicInsnNode d && d.bsm.getOwner().equals("java/lang/runtime/ObjectMethods"))
			hashCode.instructions = generateHashCode(classNode.name, classNode.recordComponents);

		MethodNode equals = methodNodes.get("equals(Ljava/lang/Object;)Z");
		if (equals.instructions.size() == 7 && equals.instructions.get(4) instanceof InvokeDynamicInsnNode d && d.bsm.getOwner().equals("java/lang/runtime/ObjectMethods"))
			equals.instructions = generateEquals(classNode.name, classNode.recordComponents);

		// Replace super.<init> call
		MethodNode init = methodNodes.get("<init>(" + classNode.recordComponents.stream().map(n -> n.descriptor).collect(Collectors.joining()) + ")V");
		replaceSuperInit(init);

		// Fix getters
		for (RecordComponentNode r : classNode.recordComponents) {
			MethodNode getter = methodNodes.get(r.name + "()" + r.descriptor);
			if (getter != null && getter.instructions.size() == 0)
				getter.instructions = generateGetter(classNode.name, r.name, r.descriptor);
		}

		return true;
	}

	private static InsnList generateGetter(String owner, String name, String desc) {
		InsnList list = new InsnList();
		list.add(new VarInsnNode(ALOAD, 0));
		list.add(new FieldInsnNode(GETFIELD, owner, name, desc));
		list.add(new InsnNode(Type.getType(desc).getOpcode(IRETURN)));
		return list;
	}

	private static void replaceSuperInit(MethodNode node) {
		ListIterator<AbstractInsnNode> it = node.instructions.iterator();
		while (it.hasNext()) {
			if (it.next().getOpcode() == INVOKESPECIAL) {
				it.set(new MethodInsnNode(INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
				return;
			}
		}
	}

	private static InsnList generateToString(String owner, List<RecordComponentNode> components) {
		InsnList list = new InsnList();

		String ownerString = owner.contains("/") ? owner.substring(owner.lastIndexOf('/') + 1) : owner;
		String formatString = ownerString + '[' + components.stream().map(c -> c.name + "=%s").collect(Collectors.joining(", ")) + ']';

		list.add(new LdcInsnNode(formatString));
		list.add(recordFieldsAsArray(owner, components));

		list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/String", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"));
		list.add(new InsnNode(ARETURN));
		return list;
	}

	private static InsnList generateHashCode(String owner, List<RecordComponentNode> components) {
		InsnList list = new InsnList();

		list.add(recordFieldsAsArray(owner, components));
		list.add(new MethodInsnNode(INVOKESTATIC, "java/util/Arrays", "hashCode", "([Ljava/lang/Object;)I"));
		list.add(new InsnNode(IRETURN));

		return list;
	}

	private static InsnList generateEquals(String owner, List<RecordComponentNode> components) {
		InsnList list = new InsnList();

		// if (this == other) return true;
		LabelNode b = new LabelNode();
		list.add(new VarInsnNode(ALOAD, 1));
		list.add(new VarInsnNode(ALOAD, 0));
		list.add(new JumpInsnNode(IF_ACMPNE, b));
		list.add(new InsnNode(ICONST_1));
		list.add(new InsnNode(IRETURN));
		list.add(b);

		// if (equals == null || equals.getClass() != equals2.getClass()) return false;
		LabelNode c = new LabelNode();
		LabelNode d = new LabelNode();
		list.add(new VarInsnNode(ALOAD, 1));
		list.add(new JumpInsnNode(IFNULL, c));
		list.add(new VarInsnNode(ALOAD, 1));
		list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;"));
		list.add(new VarInsnNode(ALOAD, 0));
		list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;"));
		list.add(new JumpInsnNode(IF_ACMPEQ, d));
		list.add(c);
		list.add(new InsnNode(ICONST_0));
		list.add(new InsnNode(IRETURN));

		list.add(d);
		list.add(new VarInsnNode(ALOAD, 1));
		list.add(new TypeInsnNode(CHECKCAST, owner));
		list.add(new VarInsnNode(ASTORE, 2));

		LabelNode fail = new LabelNode();
		for (RecordComponentNode component : components) {
			Type type = Type.getType(component.descriptor);
			if (type.getSort() == Type.FLOAT) {
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new FieldInsnNode(GETFIELD, owner, component.name, component.descriptor));
				list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I"));
				list.add(new VarInsnNode(ALOAD, 2));
				list.add(new FieldInsnNode(GETFIELD, owner, component.name, component.descriptor));
				list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I"));
				list.add(new JumpInsnNode(IF_ICMPNE, fail));
				continue;
			} else if (type.getSort() == Type.DOUBLE) {
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new FieldInsnNode(GETFIELD, owner, component.name, component.descriptor));
				list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Double", "doubleToLongBits", "(D)J"));
				list.add(new VarInsnNode(ALOAD, 2));
				list.add(new FieldInsnNode(GETFIELD, owner, component.name, component.descriptor));
				list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Double", "doubleToLongBits", "(D)J"));
				list.add(new InsnNode(LCMP));
				list.add(new JumpInsnNode(IFNE, fail));
				continue;
			}

			list.add(new VarInsnNode(ALOAD, 0));
			list.add(new FieldInsnNode(GETFIELD, owner, component.name, component.descriptor));
			list.add(new VarInsnNode(ALOAD, 2));
			list.add(new FieldInsnNode(GETFIELD, owner, component.name, component.descriptor));

			if (type.getSort() == Type.LONG) {
				list.add(new InsnNode(LCMP));
				list.add(new JumpInsnNode(IFNE, fail));
			} else if (type.getSort() < Type.ARRAY) {
				list.add(new JumpInsnNode(IF_ICMPNE, fail));
			} else {
				list.add(new MethodInsnNode(INVOKESTATIC, "java/util/Objects", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z"));
				list.add(new JumpInsnNode(IFEQ, fail));
			}
		}
		LabelNode success = new LabelNode();
		list.add(new InsnNode(ICONST_1));
		list.add(new JumpInsnNode(GOTO, success));

		list.add(fail);
		list.add(new InsnNode(ICONST_0));
		list.add(success);
		list.add(new InsnNode(IRETURN));

		return list;
	}

	private static InsnList recordFieldsAsArray(String owner, List<RecordComponentNode> components) {
		InsnList list = new InsnList();

		list.add(getNumberInsn(components.size()));
		list.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));

		int i = 0;
		for (RecordComponentNode component : components) {
			list.add(new InsnNode(DUP));

			list.add(getNumberInsn(i++));
			list.add(new VarInsnNode(ALOAD, 0));
			list.add(new FieldInsnNode(GETFIELD, owner, component.name, component.descriptor));

			Type type = Type.getType(component.descriptor);
			if (type.getSort() < ARRAY)
				list.add(CONVERTERS[type.getSort() - 1].valueOf());

			list.add(new InsnNode(AASTORE));
		}

		return list;
	}

	private static final class Converter {

		private final String wrapper;
		private final char descChar;

		private Converter(String wrapper, char descChar) {
			this.wrapper = wrapper;
			this.descChar = descChar;
		}

		public MethodInsnNode valueOf() {
			return new MethodInsnNode(INVOKESTATIC, "java/lang/" + wrapper, "valueOf", "(" + descChar + ")Ljava/lang/" + wrapper + ";");
		}

	}

	private static AbstractInsnNode getNumberInsn(int num) {
		if (num == -1)
			return new InsnNode(ICONST_M1);
		if (num >= 0 && num <= 5)
			return new InsnNode(ICONST_0 + num);

		if ((byte) num == num)
			return new IntInsnNode(BIPUSH, num);
		if ((short) num == num)
			return new IntInsnNode(SIPUSH, num);

		return new LdcInsnNode(num);
	}

}
