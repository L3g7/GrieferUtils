/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.scripts;

import dev.l3g7.griefer_utils.features.uncategorized.scripts.Scripts.ScriptSyntaxException;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

class Script {

	public static ClassNode load(Iterable<String> lines) throws ScriptSyntaxException {
		Script loader = new Script();

		loader.getLocal("args");

		Iterator<String> iterator = lines.iterator();
		AtomicInteger lineNumber = new AtomicInteger(0);
		while (iterator.hasNext()) {
			if (lineNumber.incrementAndGet() < (1<<16)) {
				LabelNode label = new LabelNode();
				loader.method.instructions.add(label);
				loader.method.instructions.add(new LineNumberNode(lineNumber.get(), label));
			}

			String line = iterator.next().trim();
			if (line.isEmpty() || line.startsWith("//"))
				continue;

			try {
				loader.processTokens(lineNumber, iterator, line.split(" "));
			} catch (ScriptSyntaxException e) {
				e.setLine(lineNumber.get(), line);
				throw e;
			}
		}

		// Generate class node
		ClassNode classNode = new ClassNode();
		classNode.name = loader.className;
		classNode.superName = "java/lang/Object";
		classNode.access = ACC_PUBLIC;
		classNode.version = V1_8;
		classNode.methods.add(loader.method);

		for (Map.Entry<String, String> entry : loader.globals.entrySet())
			classNode.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC, entry.getKey(), entry.getValue(), null, null));

		return classNode;
	}

	TokenPhase currentPhase = TokenPhase.GLOBALS;

	// Name -> Type
	public final Map<String, String> globals = new LinkedHashMap<>();
	private final Map<String, LabelNode> labels = new HashMap<>();
	private final List<String> locals = new ArrayList<>();
	public final MethodNode method = new MethodNode(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/Object;)V", null, null);
	public final String className = "dev/l3g7/griefer_utils/features/uncategorized/scripts/LoadedScript" + new Random().nextInt();

	int getLocal(String name) {
		if (!locals.contains(name))
			locals.add(name);

		return locals.indexOf(name);
	}

	LabelNode getLabel(String name) {
		return labels.computeIfAbsent(name.toLowerCase(), k -> new LabelNode());
	}

	private void processTokens(AtomicInteger lineNumber, Iterator<String> iterator, String[] tokens) throws ScriptSyntaxException {
		String opcode = tokens[0] = tokens[0].toLowerCase();

		// Process labels
		if (opcode.endsWith(":")) {
			method.instructions.add(getLabel(opcode.substring(0, opcode.length() - 1)));
			return;
		}

		if (tokens[tokens.length - 1].equals("{")) {
			try {
				MultiLineOpcodes.process(this, lineNumber, iterator, tokens);
			} catch (Throwable t) {
				if (t instanceof ScriptSyntaxException s)
					throw s;

				throw new ScriptSyntaxException(t.getMessage(), t);
			}
			return;
		}

		OpcodeProcessors.Opcode processor = OpcodeProcessors.PROCESSORS.get(opcode);
		if (processor == null)
			throw new ScriptSyntaxException("Unbekannter Opcode " + opcode);

		if (processor.phase().ordinal() < currentPhase.ordinal())
			throw new ScriptSyntaxException(currentPhase + " erwartet, " + processor.phase() + " gefunden");

		if (processor.requiredTokens() >= 0 && processor.requiredTokens() != tokens.length - 1)
			throw new ScriptSyntaxException("Ungültigige Anzahl an Argumenten");

		currentPhase = processor.phase();
		try {
			processor.processor().acceptWithThrowable(this, tokens);
		} catch (Throwable t) {
			if (t instanceof ScriptSyntaxException s)
				throw s;

			throw new ScriptSyntaxException(t.getMessage(), t);
		}
	}

	public enum TokenPhase {
		GLOBALS,
		TRY_CATCHES,
		CODE
	}

}
