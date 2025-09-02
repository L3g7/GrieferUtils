package dev.l3g7.griefer_utils.features.uncategorized.scripts;

import dev.l3g7.griefer_utils.core.api.misc.functions.BiConsumer;
import dev.l3g7.griefer_utils.core.api.misc.functions.TriFunction;
import dev.l3g7.griefer_utils.features.uncategorized.scripts.Scripts.ScriptSyntaxException;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

import static dev.l3g7.griefer_utils.features.uncategorized.scripts.Util.getNumberInsn;
import static dev.l3g7.griefer_utils.features.uncategorized.scripts.Util.removeTrailingComma;
import static org.objectweb.asm.Opcodes.*;

class OpcodeProcessors {

	public static final Map<String, Opcode> PROCESSORS = new HashMap<>();
	private static final Map<String, Integer> OPCODES = new HashMap<>();

	private static void code(String name, int requiredTokens, TriFunction<Script, Integer, String[], AbstractInsnNode> processor) {
		code(requiredTokens, processor, name);
	}

	private static void code(int requiredTokens, TriFunction<Script, Integer, String[], AbstractInsnNode> processor, String... names) {
		Opcode op = new Opcode(Script.TokenPhase.CODE, requiredTokens, (script, tokens) -> {
			String[] args = new String[tokens.length - 1];
			System.arraycopy(tokens, 1, args, 0, tokens.length - 1);
			script.method.instructions.add(processor.applyWithThrowable(script, OPCODES.get(tokens[0]), args));
		});

		for (String name : names)
			PROCESSORS.put(name, op);
	}

	static {
		boolean foundNOP = false;
		try {
			for (Field field : Opcodes.class.getDeclaredFields()) {
				if (field.getType() != int.class)
					continue;

				if (field.getName().equals("NOP"))
					foundNOP = true;

				if (foundNOP)
					OPCODES.put(field.getName().toLowerCase(), field.getInt(null));
			}
		} catch (IllegalAccessException e) {
			throw dev.l3g7.griefer_utils.core.api.util.Util.elevate(e);
		}

		PROCESSORS.put("global", new Opcode(Script.TokenPhase.GLOBALS, 2, (script, tokens) -> {
			script.globals.put(tokens[2], switch (tokens[1]) {
				case "boolean" -> "Z";
				case "char" -> "C";
				case "byte" -> "B";
				case "short" -> "S";
				case "int" -> "I";
				case "float" -> "F";
				case "long" -> "J";
				case "double" -> "D";
				default -> 'L' + Util.resolveClass(tokens[1]) + ';';
			});
		}));
		PROCESSORS.put("trycatch", new Opcode(Script.TokenPhase.TRY_CATCHES, 4, (script, tokens) -> {
			LabelNode start = script.getLabel(removeTrailingComma(tokens[1]));
			LabelNode end = script.getLabel(removeTrailingComma(tokens[2]));
			LabelNode handler = script.getLabel(removeTrailingComma(tokens[3]));
			String exception = Util.resolveClass(tokens[4]);
			if (exception.endsWith(";"))
				exception = exception.substring(1, exception.length() - 1);
			script.method.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, exception));
		}));

		Consumer<String> globalsOpcodeGenerator = opcode -> {
			code(opcode, 1, (script, opcd, args) -> {
				String type = script.globals.get(args[0]);
				if (type == null)
					throw new ScriptSyntaxException("Unbekannte global \"" + args[0] + "\"");

				return new FieldInsnNode(opcode.equals("putglobal") ? PUTSTATIC : GETSTATIC, script.className, args[0], type);
			});
		};
		globalsOpcodeGenerator.accept("putglobal");
		globalsOpcodeGenerator.accept("getglobal");

		PROCESSORS.put("collectarray", new Opcode(Script.TokenPhase.CODE, 2, (script, args) -> {
			InsnList insns = script.method.instructions;
			int size = Integer.parseInt(args[2]);
			insns.add(getNumberInsn(size));
			insns.add(new TypeInsnNode(ANEWARRAY, args[1]));

			for (int i = size - 1; i >= 0; i--) {
				insns.add(new InsnNode(DUP_X1));
				insns.add(new InsnNode(SWAP));
				insns.add(getNumberInsn(i));
				insns.add(new InsnNode(SWAP));
				insns.add(new InsnNode(AASTORE));
			}
		}));

		PROCESSORS.put("line", new Opcode(Script.TokenPhase.CODE, -1, (script, args) -> {}));
		code("ldc", -1, (script, opcode, args) -> {
			Object constant = ConstantParser.readConstant(Arrays.asList(args).iterator());
			if (constant == null)
				throw new ScriptSyntaxException("Konstante konnte nicht gelesen werden");

			return new LdcInsnNode(constant);
		});

		code("iinc", 2, (script, opcode, args) -> new IincInsnNode(script.getLocal(args[0], false), Integer.parseInt(args[1])));
		for (String opcode : new String[]{"iload", "lload", "fload", "dload", "aload", "istore", "lstore", "fstore", "dstore", "astore", "ret"}) {
			PROCESSORS.put(opcode, new Opcode(Script.TokenPhase.CODE, 1, (script, tokens) -> {
				boolean isWide = tokens[0].startsWith("l") || tokens[0].startsWith("d");
				script.method.instructions.add(new VarInsnNode(OPCODES.get(tokens[0]), script.getLocal(tokens[1], isWide)));
			}));
		}

		// FieldInsnNode
		code(2, (script, opcode, args) -> {
			String name = args[0];
			String[] parts = name.split("\\.");
			return new FieldInsnNode(opcode, parts[0], parts[1], args[1]);
		}, "getstatic", "putstatic", "getfield", "putfield");

		// TypeInsnNode
		code(1, (script, opcode, args) -> new TypeInsnNode(opcode, args[0]),
			"new", "anewarray", "checkcast", "instanceof");

		// IntInsnNode
		code(1, (script, opcode, args) -> new IntInsnNode(opcode, Integer.parseInt(args[0])),
			"bipush", "sipush", "newarray");

		code("multianewarray", 2, (script, opcode, args) -> new MultiANewArrayInsnNode(args[0], Integer.parseInt(args[1])));

		// JumpInsnNode
		code(1, (script, opcode, args) -> new JumpInsnNode(opcode, script.getLabel(args[0])),
			"ifeq", "ifne", "iflt", "ifge", "ifgt", "ifle", "if_icmpeq", "if_icmpne", "if_icmplt", "if_icmpge", "if_icmpgt", "if_icmple", "if_acmpeq", "if_acmpne", "goto", "jsr", "ifnull", "ifnonnull");

		// MethodInsnNode
		code(2, (script, opcode, args) -> {
			String[] ownerAndName = args[0].split("\\.");
			return new MethodInsnNode(opcode, ownerAndName[0], ownerAndName[1], args[1]);
		}, "invokevirtual", "invokespecial", "invokestatic", "invokeinterface");
		code("invokestaticinterface", 2, (script, opcode, args) -> {
			String[] ownerAndName = args[0].split("\\.");
			return new MethodInsnNode(INVOKESTATIC, ownerAndName[0], ownerAndName[1], args[1], true);
		});

		// InvokeDynamicInsnNode
		code("invokedynamic", -1, (script, opcode, args) -> {
			if (args.length < 5)
				throw new ScriptSyntaxException("Zu wenig Argumente");

			Iterator<String> iterator = Arrays.asList(args).iterator();
			String name = iterator.next();
			String desc = iterator.next();


			String[] bsmOwnerAndName = iterator.next().split("\\.");
			if (bsmOwnerAndName.length != 2)
				throw new ScriptSyntaxException("Ungültige BSM Syntax");

			String bsmOwner = Util.resolveClass(bsmOwnerAndName[0]);
			String bsmName = bsmOwnerAndName[1];
			Method bsmMethod = null;
			try {
				Class<?> bsmClass = Class.forName(bsmOwner.replace('/', '.'));
				for (Method declaredMethod : bsmClass.getDeclaredMethods()) {
					if (!declaredMethod.getName().equals(bsmName))
						continue;

					if (bsmMethod != null)
						throw new ScriptSyntaxException("Überladene BSMs sind nicht unterstützt");

					bsmMethod = declaredMethod;
				}

				if (bsmMethod == null)
					throw new ScriptSyntaxException("Es konnte kein BSM mit dem Namen " + bsmName + " gefunden werden");
			} catch (ReflectiveOperationException e) {
				throw new ScriptSyntaxException("BSM descriptor konnte nicht ermittelt werden", e);
			}
			Handle bsm = new Handle(H_INVOKESTATIC, bsmOwner, bsmName, Type.getMethodDescriptor(bsmMethod), bsmMethod.getDeclaringClass().isInterface());

			List<Object> bsmArgs = new ArrayList<>();
			if (!iterator.next().equals("{"))
				throw new ScriptSyntaxException("Ungültige BSM-Argument Syntax");

			while (iterator.hasNext()) {
				String token = iterator.next();
				if (token.equals("}"))
					break;

				Object constant = ConstantParser.readConstant(token, iterator);
				if (constant == null)
					throw new ScriptSyntaxException("Konstante konnte nicht gelesen werden");

				bsmArgs.add(constant);
			}

			if (iterator.hasNext())
				throw new ScriptSyntaxException("Zu viele Argumente");

			return new InvokeDynamicInsnNode(name, desc, bsm, bsmArgs.toArray());
		});

		// Add missing opcodes as InsnNode
		OPCODES.keySet().stream()
			.filter(opcode -> !PROCESSORS.containsKey(opcode))
			.forEach(opcode -> code(opcode.toLowerCase(), 0, (script, opc, args) -> new InsnNode(opc)));
	}

	public record Opcode(Script.TokenPhase phase, int requiredTokens, BiConsumer<Script, String[]> processor) {}

}
