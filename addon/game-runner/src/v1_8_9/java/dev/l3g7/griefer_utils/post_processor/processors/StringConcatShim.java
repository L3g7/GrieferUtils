/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors;

import dev.l3g7.griefer_utils.post_processor.LatePostProcessor.Processor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
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

import static java.lang.invoke.MethodType.methodType;

/**
 * Redirects StringConcatFactory.makeConcatWithConstants calls to a shim.
 */
public class StringConcatShim extends Processor implements Opcodes {

	private static final String BOOTSTRAP_MTD = "java/lang/invoke/StringConcatFactory.makeConcatWithConstants(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; (6)";

	private static final MethodHandle mhRebind, mhEditor, mhBasicType,
		mhFoldArgumentsForm, mhCopyWithExtendL;

	@Override
	public void process(ClassNode classNode) {
		for (MethodNode method : classNode.methods) {
			for (AbstractInsnNode node : method.instructions) {
				if (!(node instanceof InvokeDynamicInsnNode e))
					continue;

				if (!e.name.equals("makeConcatWithConstants") || !e.bsm.toString().equals(BOOTSTRAP_MTD))
					continue;

				// noinspection deprecation // required for Java 8 compatibility
				e.bsm = new Handle(6,
					StringConcatShim.class.getName().replace('.', '/'),
					e.bsm.getName(), e.bsm.getDesc()
				);
				setModified();
			}
		}
	}

	static {
		try {
			// Create elevated lookup
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			Unsafe unsafe = (Unsafe) theUnsafe.get(null);
			unsafe.putInt(lookup, 12 /* allowedModes */, -1 /* TRUSTED */);

			// Find private classes
			Class<?> boundMethodHandle = Class.forName("java.lang.invoke.BoundMethodHandle");
			Class<?> lambdaFormEditor = Class.forName("java.lang.invoke.LambdaFormEditor");
			Class<?> lambdaForm = Class.forName("java.lang.invoke.LambdaForm");

			// Find method handles
			mhRebind = lookup.findVirtual(MethodHandle.class, "rebind", methodType(boundMethodHandle));
			mhEditor = lookup.findVirtual(boundMethodHandle, "editor", methodType(lambdaFormEditor));
			mhBasicType = lookup.findVirtual(MethodType.class, "basicType", methodType(MethodType.class));
			mhFoldArgumentsForm = lookup.findVirtual(lambdaFormEditor, "foldArgumentsForm", methodType(lambdaForm, int.class, boolean.class, MethodType.class));
			mhCopyWithExtendL = lookup.findVirtual(boundMethodHandle, "copyWithExtendL", methodType(boundMethodHandle, MethodType.class, lambdaForm, Object.class));
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
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
		MethodHandle result = lookup.findStatic(StringConcatShim.class, "build", methodType(String.class, StringBuilder.class));
		// result has type (StringBuilder) -> String

		for (String element : elements) {
			MethodHandle insert = lookup.findStatic(StringConcatShim.class, "insert", methodType(StringBuilder.class, Object.class, StringBuilder.class));
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

}
