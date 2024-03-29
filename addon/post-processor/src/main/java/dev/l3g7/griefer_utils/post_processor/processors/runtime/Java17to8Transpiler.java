/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.runtime;

import dev.l3g7.griefer_utils.post_processor.processors.RuntimePostProcessor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;
import sun.misc.Unsafe;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.invoke.MethodType.methodType;

/**
 * Overwrites the class file version and redirects StringConcatFactory.makeConcatWithConstants calls to a shim.
 */
public class Java17to8Transpiler extends RuntimePostProcessor {

	public static final Java17to8Transpiler INSTANCE = new Java17to8Transpiler();

	private static final String BOOTSTRAP_MTD = "java/lang/invoke/StringConcatFactory.makeConcatWithConstants(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; (6)";

	private static final MethodHandle mhRebind, mhEditor, mhBasicType,
		mhFoldArgumentsForm, mhCopyWithExtendL;

	static {
		try {
			// Create elevated lookup
			MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			Unsafe unsafe = (Unsafe) theUnsafe.get(null);
			unsafe.putInt(LOOKUP, 12 /* allowedModes */, -1 /* TRUSTED */);

			// Find private classes
			Class<?> boundMethodHandle = Class.forName("java.lang.invoke.BoundMethodHandle");
			Class<?> lambdaFormEditor = Class.forName("java.lang.invoke.LambdaFormEditor");
			Class<?> lambdaForm = Class.forName("java.lang.invoke.LambdaForm");

			// Find method handles
			mhRebind = LOOKUP.findVirtual(MethodHandle.class, "rebind", methodType(boundMethodHandle));
			mhEditor = LOOKUP.findVirtual(boundMethodHandle, "editor", methodType(lambdaFormEditor));
			mhBasicType = LOOKUP.findVirtual(MethodType.class, "basicType", methodType(MethodType.class));
			mhFoldArgumentsForm = LOOKUP.findVirtual(lambdaFormEditor, "foldArgumentsForm", methodType(lambdaForm, int.class, boolean.class, MethodType.class));
			mhCopyWithExtendL = LOOKUP.findVirtual(boundMethodHandle, "copyWithExtendL", methodType(boundMethodHandle, MethodType.class, lambdaForm, Object.class));
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private Java17to8Transpiler() {}

	@Override
	public byte[] transform(String fileName, byte[] classBytes) {
		if (!fileName.startsWith("dev/l3g7/griefer_utils/") || !fileName.endsWith(".class"))
			return classBytes;

		classBytes[7 /* major_version */] = 52 /* Java 1.8 */;
		ClassNode classNode = new ClassNode();
		ClassReader reader = new ClassReader(classBytes);
		reader.accept(classNode, 0);

		// Check all instructions
		AtomicBoolean modified = new AtomicBoolean(false);
		for (MethodNode method : classNode.methods) {
			method.instructions.iterator().forEachRemaining(node -> {
				if (!(node instanceof InvokeDynamicInsnNode e))
					return;

				if (!e.name.equals("makeConcatWithConstants") || !e.bsm.toString().equals(BOOTSTRAP_MTD))
					return;

				// Patch InvokeDynamic
				// noinspection deprecation // required for Java 8 compatibility
				e.bsm = new Handle(6,
					Java17to8Transpiler.class.getName().replace('.', '/'),
					e.bsm.getName(), e.bsm.getDesc()
				);

				modified.set(true);
			});
		}

		// Write modified class
		if (!modified.get())
			return classBytes;

		ClassWriter writer = new BoundClassWriter();
		classNode.accept(writer);
		return writer.toByteArray();
	}

	@SuppressWarnings("unused") // Invoked dynamically
	public static CallSite makeConcatWithConstants(MethodHandles.Lookup lookup,
	                                               String name,
	                                               MethodType concatType,
	                                               String recipe,
	                                               Object... constants) throws Throwable {
		if (recipe.contains("\u0002"))
			throw new UnsupportedOperationException("Unimplemented recipe: " + Base64.getEncoder().encodeToString(recipe.getBytes(StandardCharsets.UTF_8)));

		// Parse recipe
		List<String> elements = new ArrayList<>();
		int lastIdx = 0;
		for (int i = 0; i < recipe.length(); i++) {
			if (recipe.charAt(i) == '\u0001') {
				if (lastIdx != i)
					elements.add(recipe.substring(lastIdx, i));

				lastIdx = i + 1;
				elements.add(null);
			}
		}
		if (lastIdx != recipe.length())
			elements.add(recipe.substring(lastIdx));

		// Create MethodHandle
		MethodHandle result = lookup.findStatic(Java17to8Transpiler.class, "build", methodType(String.class, StringBuilder.class));
		// result has type (StringBuilder) -> String

		for (String element : elements) {
			MethodHandle insert = lookup.findStatic(Java17to8Transpiler.class, "insert", methodType(StringBuilder.class, Object.class, StringBuilder.class));
			// insert has type (Object, StringBuilder) -> StringBuilder

			if (element == null) {
				// Call insert using new argument
				result = MethodHandles.collectArguments(result, result.type().parameterCount() - 1, insert);
				// result gets type (..., arg, StringBuilder) -> String
			} else {
				// Bind first constant
				insert = MethodHandles.insertArguments(insert, 0, element);
				// insert gets type (StringBuilder) -> StringBuilder

				// Call insert
				result = MethodHandles.filterArguments(result, result.type().parameterCount() - 1, insert);
				// result type doesn't change
			}
		}

		// Insert StringBuilder
		MethodHandle construct = lookup.findConstructor(StringBuilder.class, methodType(void.class));
		result = foldArguments(result, result.type().parameterCount() - 1, construct);

		// Cast types
		result = result.asType(concatType);

		return new ConstantCallSite(result);
	}

	/**
	 * Tailored reimplementation of {@link MethodHandles#foldArguments(MethodHandle, int, MethodHandle)}, since it doesn't exist in Java 8.
	 */
	private static MethodHandle foldArguments(MethodHandle target, int pos, MethodHandle combiner) throws Throwable {
		Object result = mhRebind.invoke(target);
		Object form = mhFoldArgumentsForm.invoke(mhEditor.invoke(result), pos + 1, false, mhBasicType.invoke(combiner.type()));
		MethodType targetType = target.type().dropParameterTypes(pos, pos + 1);
		return (MethodHandle) mhCopyWithExtendL.invoke(result, targetType, form, combiner);
	}

	// Helper methods

	public static StringBuilder insert(Object value, StringBuilder v) {
		return v.insert(0, value);
	}

	public static String build(StringBuilder b) {
		return b.toString();
	}

	/**
	 * A ClassWriter loaded by the same ClassLoader as the transformer. As {@link ClassWriter} is normally
	 * loaded by the parent ClassLoader of the one loading this addon, it wouldn't find the classes
	 * defined by its child ClassLoader and getCommonSuperClass calls would fail.
	 */
	private static class BoundClassWriter extends ClassWriter {
		public BoundClassWriter() {
			super(COMPUTE_MAXS | COMPUTE_FRAMES);
		}
	}

}
