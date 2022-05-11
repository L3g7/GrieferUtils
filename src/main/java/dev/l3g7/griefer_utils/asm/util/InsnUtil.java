package dev.l3g7.griefer_utils.asm.util;

import dev.l3g7.griefer_utils.asm.mappings.MappingNode;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;

public class InsnUtil {

	static final Map<String, LabelNode> labelStack = new HashMap<>();

	public static VarInsnNode varInsn(int opcode, int var) {
		return new VarInsnNode(opcode, var);
	}

	public static MethodInsnNode methodInsn(int opcode, MappingNode.Method mapping) {
		return new MethodInsnNode(opcode, mapping.owner.toString(), mapping.toString(), ASMUtil.getDescription(mapping), false);
	}

	public static JumpInsnNode jumpInsn(int opcode, String label) {
		return new JumpInsnNode(opcode, label(label));
	}

	public static JumpInsnNode jumpInsn(int opcode, JumpInsnNode target) {
		return new JumpInsnNode(opcode, target.label);
	}

	public static LabelNode label(String label) {
		return labelStack.computeIfAbsent(label, s -> new LabelNode());
	}

	public static InsnNode insn(int opcode) {
		return new InsnNode(opcode);
	}
}
