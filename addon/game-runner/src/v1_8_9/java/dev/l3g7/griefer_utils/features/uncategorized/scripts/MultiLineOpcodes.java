package dev.l3g7.griefer_utils.features.uncategorized.scripts;

import dev.l3g7.griefer_utils.core.api.misc.functions.BiFunction;
import dev.l3g7.griefer_utils.features.uncategorized.scripts.Scripts.ScriptSyntaxException;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class MultiLineOpcodes {

	private static final Map<String, BiFunction<Script, Map<String, String>, AbstractInsnNode>> OPCODES = new HashMap<>();

	static {
		OPCODES.put("tableswitch", (script, args) -> {
			int min = Integer.parseInt(args.get("min"));
			int max = Integer.parseInt(args.get("max"));
			LabelNode dflt = script.getLabel(args.get("default"));

			String allCases = args.get("cases");
			String[] cases = allCases.substring(1, allCases.length() - 1).split(",");
			LabelNode[] caseLabels = Arrays.stream(cases).map(label -> script.getLabel(label.trim())).toArray(LabelNode[]::new);
			return new TableSwitchInsnNode(min, max, dflt, caseLabels);
		});

		OPCODES.put("lookupswitch", (script, args) -> {
			int[] keys = new int[args.size() - 1];
			LabelNode[] values = new LabelNode[args.size() - 1];

			int i = 0;
			for (Map.Entry<String, String> entry : args.entrySet()) {
				if (entry.getKey().equals("default"))
					continue;

				keys[i] = Integer.parseInt(entry.getKey());
				values[i++] = script.getLabel(entry.getValue());
			}

			return new LookupSwitchInsnNode(script.getLabel(args.get("default")), keys, values);
		});
	}

	public static void process(Script script, AtomicInteger lineNumber, Iterator<String> iterator, String[] tokens) throws Throwable {
		BiFunction<Script, Map<String, String>, AbstractInsnNode> processor = OPCODES.get(tokens[0].toLowerCase());
		if (processor == null) {
			throw new ScriptSyntaxException("Unbekannter Opcode " + tokens[0]);
		}

		script.currentPhase = Script.TokenPhase.CODE;
		Map<String, String> args = collectArgs(iterator);
		script.method.instructions.add(processor.applyWithThrowable(script, args));

		lineNumber.getAndAdd(args.size() + 2);
	}

	private static Map<String, String> collectArgs(Iterator<String> iterator) throws ScriptSyntaxException {
		Map<String, String> args = new HashMap<>();

		String line = null;
		while (iterator.hasNext() && !(line = iterator.next().trim()).equals("}")) {
			if (line.isEmpty() || line.startsWith("//"))
				continue;

			int colon = line.indexOf(":");
			if (colon == -1)
				throw new ScriptSyntaxException("Ungültiges KV Paar");

			String key = line.substring(0, colon);
			String value = line.substring(colon + 1);
			if (value.endsWith(","))
				value = value.substring(0, value.length() - 1);

			if (args.put(key.trim(), value.trim()) != null)
				throw new ScriptSyntaxException("Doppelter Schüssel " + key);
		}

		if (!"}".equals(line))
			throw new ScriptSyntaxException("\"}\"-Zeile erwartet");

		return args;
	}

}
