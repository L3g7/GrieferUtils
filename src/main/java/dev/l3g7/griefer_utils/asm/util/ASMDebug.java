package dev.l3g7.griefer_utils.asm.util;

import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ASMDebug {

	public static void dump(InsnList l) {
		AbstractInsnNode n = l.getFirst();
		do {
			dump(n);
		} while((n = n.getNext()) != null);
	}

	public static void dump(AbstractInsnNode a) {
		if (a instanceof VarInsnNode) {
			VarInsnNode n = (VarInsnNode) a;
			System.out.println("VarInsnNode " + resolveOpcode(n, "ILOAD LLOAD FLOAD DLOAD ALOAD ISTORE LSTORE FSTORE DSTORE ASTORE RET") + " " + n.var);
		} else if (a instanceof MethodInsnNode) {
			MethodInsnNode n = (MethodInsnNode) a;
			System.out.println("MethodInsnNode " + resolveOpcode(n, "INVOKEVIRTUAL INVOKESPECIAL INVOKESTATIC INVOKEINTERFACE") + " " + n.owner + " " + n.name + " " + n.desc);
		} else if (a instanceof JumpInsnNode) {
			JumpInsnNode n = (JumpInsnNode) a;
			System.out.println("JumpInsnNode " + resolveOpcode(n, "IFEQ IFNE IFLT IFGE IFGT IFLE IF_ICMPEQ IF_ICMPNE IF_ICMPLT IF_ICMPGE IF_ICMPGT IF_ICMPLE IF_ACMPEQ IF_ACMPNE GOTO JSR IFNULL IFNONNULL") + " " + n.label.hashCode());
		} else if (a instanceof InsnNode) {
			InsnNode n = (InsnNode) a;
			System.out.println("InsnNode " + resolveOpcode(n, "NOP ACONST_NULL ICONST_M1 ICONST_0 ICONST_1 ICONST_2 ICONST_3 ICONST_4 ICONST_5 LCONST_0 LCONST_1 FCONST_0 FCONST_1 FCONST_2 DCONST_0 DCONST_1 IALOAD LALOAD FALOAD DALOAD AALOAD BALOAD CALOAD SALOAD IASTORE LASTORE FASTORE DASTORE AASTORE BASTORE CASTORE SASTORE POP POP2 DUP DUP_X1 DUP_X2 DUP2 DUP2_X1 DUP2_X2 SWAP IADD LADD FADD DADD ISUB LSUB FSUB DSUB IMUL LMUL FMUL DMUL IDIV LDIV FDIV DDIV IREM LREM FREM DREM INEG LNEG FNEG DNEG ISHL LSHL ISHR LSHR IUSHR LUSHR IAND LAND IOR LOR IXOR LXOR I2L I2F I2D L2I L2F L2D F2I F2L F2D D2I D2L D2F I2B I2C I2S LCMP FCMPL FCMPG DCMPL DCMPG IRETURN LRETURN FRETURN DRETURN ARETURN RETURN ARRAYLENGTH ATHROW MONITORENTER MONITOREXIT"));
		} else if (a instanceof FieldInsnNode) {
			FieldInsnNode n = (FieldInsnNode) a;
			System.out.println("FieldInsnNode " + resolveOpcode(n, "GETSTATIC PUTSTATIC GETFIELD PUTFIELD") + " " + n.owner + " " + n.name + " " + n.desc);
		} else if (a instanceof LineNumberNode) {
			LineNumberNode n = (LineNumberNode) a;
			System.out.println("LineNumberNode " + n.line);
		} else if (a instanceof LabelNode) {
			LabelNode n = (LabelNode) a;
			System.out.println("LabelNode " + n.hashCode());
		} else if (a instanceof FrameNode) {
			FrameNode n = (FrameNode) a;
			System.out.println("FrameNode " + resolveOpcode(n, "F_NEW F_FULL F_APPEND F_CHOP F_SAME F_APPEND F_SAME1") + " "
			+ (n.local == null ? "null" : ("[" + n.local.stream().map(ASMDebug::fnEntry2Str).collect(Collectors.joining(", "))) + "]") + " "
			+ (n.stack == null ? "null" : ("[" + n.stack.stream().map(ASMDebug::fnEntry2Str).collect(Collectors.joining(", "))) + "]"));
		} else
			System.out.println(a);
	}

	private static String fnEntry2Str(Object o) {
		if(o instanceof LabelNode)
			return "L" + o.hashCode();
		else if(o instanceof String)
			return "S" + o;
		else
			return "I" + o;
	}

	private static String resolveOpcode(AbstractInsnNode node, String validOpcodes) {
		for(String o : validOpcodes.split(" ")) {
			if(opcodeLookup.get(o) == node.getOpcode())
				return o;
		}
		throw new IllegalStateException("Unknown op " + node.getOpcode() + " for codes '" + validOpcodes + "' !");
	}

	static Map<String, Integer> opcodeLookup = new HashMap<>();
	static {
		opcodeLookup.put("F_NEW", -1);
		opcodeLookup.put("F_FULL", 0);
		opcodeLookup.put("F_APPEND", 1);
		opcodeLookup.put("F_CHOP", 2);
		opcodeLookup.put("F_SAME", 3);
		opcodeLookup.put("F_SAME1", 4);
		opcodeLookup.put("NOP", 0);
		opcodeLookup.put("ACONST_NULL", 1);
		opcodeLookup.put("ICONST_M1", 2);
		opcodeLookup.put("ICONST_0", 3);
		opcodeLookup.put("ICONST_1", 4);
		opcodeLookup.put("ICONST_2", 5);
		opcodeLookup.put("ICONST_3", 6);
		opcodeLookup.put("ICONST_4", 7);
		opcodeLookup.put("ICONST_5", 8);
		opcodeLookup.put("LCONST_0", 9);
		opcodeLookup.put("LCONST_1", 10);
		opcodeLookup.put("FCONST_0", 11);
		opcodeLookup.put("FCONST_1", 12);
		opcodeLookup.put("FCONST_2", 13);
		opcodeLookup.put("DCONST_0", 14);
		opcodeLookup.put("DCONST_1", 15);
		opcodeLookup.put("BIPUSH", 16);
		opcodeLookup.put("SIPUSH", 17);
		opcodeLookup.put("LDC", 18);
		opcodeLookup.put("ILOAD", 21);
		opcodeLookup.put("LLOAD", 22);
		opcodeLookup.put("FLOAD", 23);
		opcodeLookup.put("DLOAD", 24);
		opcodeLookup.put("ALOAD", 25);
		opcodeLookup.put("IALOAD", 46);
		opcodeLookup.put("LALOAD", 47);
		opcodeLookup.put("FALOAD", 48);
		opcodeLookup.put("DALOAD", 49);
		opcodeLookup.put("AALOAD", 50);
		opcodeLookup.put("BALOAD", 51);
		opcodeLookup.put("CALOAD", 52);
		opcodeLookup.put("SALOAD", 53);
		opcodeLookup.put("ISTORE", 54);
		opcodeLookup.put("LSTORE", 55);
		opcodeLookup.put("FSTORE", 56);
		opcodeLookup.put("DSTORE", 57);
		opcodeLookup.put("ASTORE", 58);
		opcodeLookup.put("IASTORE", 79);
		opcodeLookup.put("LASTORE", 80);
		opcodeLookup.put("FASTORE", 81);
		opcodeLookup.put("DASTORE", 82);
		opcodeLookup.put("AASTORE", 83);
		opcodeLookup.put("BASTORE", 84);
		opcodeLookup.put("CASTORE", 85);
		opcodeLookup.put("SASTORE", 86);
		opcodeLookup.put("POP", 87);
		opcodeLookup.put("POP2", 88);
		opcodeLookup.put("DUP", 89);
		opcodeLookup.put("DUP_X1", 90);
		opcodeLookup.put("DUP_X2", 91);
		opcodeLookup.put("DUP2", 92);
		opcodeLookup.put("DUP2_X1", 93);
		opcodeLookup.put("DUP2_X2", 94);
		opcodeLookup.put("SWAP", 95);
		opcodeLookup.put("IADD", 96);
		opcodeLookup.put("LADD", 97);
		opcodeLookup.put("FADD", 98);
		opcodeLookup.put("DADD", 99);
		opcodeLookup.put("ISUB", 100);
		opcodeLookup.put("LSUB", 101);
		opcodeLookup.put("FSUB", 102);
		opcodeLookup.put("DSUB", 103);
		opcodeLookup.put("IMUL", 104);
		opcodeLookup.put("LMUL", 105);
		opcodeLookup.put("FMUL", 106);
		opcodeLookup.put("DMUL", 107);
		opcodeLookup.put("IDIV", 108);
		opcodeLookup.put("LDIV", 109);
		opcodeLookup.put("FDIV", 110);
		opcodeLookup.put("DDIV", 111);
		opcodeLookup.put("IREM", 112);
		opcodeLookup.put("LREM", 113);
		opcodeLookup.put("FREM", 114);
		opcodeLookup.put("DREM", 115);
		opcodeLookup.put("INEG", 116);
		opcodeLookup.put("LNEG", 117);
		opcodeLookup.put("FNEG", 118);
		opcodeLookup.put("DNEG", 119);
		opcodeLookup.put("ISHL", 120);
		opcodeLookup.put("LSHL", 121);
		opcodeLookup.put("ISHR", 122);
		opcodeLookup.put("LSHR", 123);
		opcodeLookup.put("IUSHR", 124);
		opcodeLookup.put("LUSHR", 125);
		opcodeLookup.put("IAND", 126);
		opcodeLookup.put("LAND", 127);
		opcodeLookup.put("IOR", 128);
		opcodeLookup.put("LOR", 129);
		opcodeLookup.put("IXOR", 130);
		opcodeLookup.put("LXOR", 131);
		opcodeLookup.put("IINC", 132);
		opcodeLookup.put("I2L", 133);
		opcodeLookup.put("I2F", 134);
		opcodeLookup.put("I2D", 135);
		opcodeLookup.put("L2I", 136);
		opcodeLookup.put("L2F", 137);
		opcodeLookup.put("L2D", 138);
		opcodeLookup.put("F2I", 139);
		opcodeLookup.put("F2L", 140);
		opcodeLookup.put("F2D", 141);
		opcodeLookup.put("D2I", 142);
		opcodeLookup.put("D2L", 143);
		opcodeLookup.put("D2F", 144);
		opcodeLookup.put("I2B", 145);
		opcodeLookup.put("I2C", 146);
		opcodeLookup.put("I2S", 147);
		opcodeLookup.put("LCMP", 148);
		opcodeLookup.put("FCMPL", 149);
		opcodeLookup.put("FCMPG", 150);
		opcodeLookup.put("DCMPL", 151);
		opcodeLookup.put("DCMPG", 152);
		opcodeLookup.put("IFEQ", 153);
		opcodeLookup.put("IFNE", 154);
		opcodeLookup.put("IFLT", 155);
		opcodeLookup.put("IFGE", 156);
		opcodeLookup.put("IFGT", 157);
		opcodeLookup.put("IFLE", 158);
		opcodeLookup.put("IF_ICMPEQ", 159);
		opcodeLookup.put("IF_ICMPNE", 160);
		opcodeLookup.put("IF_ICMPLT", 161);
		opcodeLookup.put("IF_ICMPGE", 162);
		opcodeLookup.put("IF_ICMPGT", 163);
		opcodeLookup.put("IF_ICMPLE", 164);
		opcodeLookup.put("IF_ACMPEQ", 165);
		opcodeLookup.put("IF_ACMPNE", 166);
		opcodeLookup.put("GOTO", 167);
		opcodeLookup.put("JSR", 168);
		opcodeLookup.put("RET", 169);
		opcodeLookup.put("TABLESWITCH", 170);
		opcodeLookup.put("LOOKUPSWITCH", 171);
		opcodeLookup.put("IRETURN", 172);
		opcodeLookup.put("LRETURN", 173);
		opcodeLookup.put("FRETURN", 174);
		opcodeLookup.put("DRETURN", 175);
		opcodeLookup.put("ARETURN", 176);
		opcodeLookup.put("RETURN", 177);
		opcodeLookup.put("GETSTATIC", 178);
		opcodeLookup.put("PUTSTATIC", 179);
		opcodeLookup.put("GETFIELD", 180);
		opcodeLookup.put("PUTFIELD", 181);
		opcodeLookup.put("INVOKEVIRTUAL", 182);
		opcodeLookup.put("INVOKESPECIAL", 183);
		opcodeLookup.put("INVOKESTATIC", 184);
		opcodeLookup.put("INVOKEINTERFACE", 185);
		opcodeLookup.put("INVOKEDYNAMIC", 186);
		opcodeLookup.put("NEW", 187);
		opcodeLookup.put("NEWARRAY", 188);
		opcodeLookup.put("ANEWARRAY", 189);
		opcodeLookup.put("ARRAYLENGTH", 190);
		opcodeLookup.put("ATHROW", 191);
		opcodeLookup.put("CHECKCAST", 192);
		opcodeLookup.put("INSTANCEOF", 193);
		opcodeLookup.put("MONITORENTER", 194);
		opcodeLookup.put("MONITOREXIT", 195);
		opcodeLookup.put("MULTIANEWARRAY", 197);
		opcodeLookup.put("IFNULL", 198);
		opcodeLookup.put("IFNONNULL", 199);
	}
}
