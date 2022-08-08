package dev.l3g7.griefer_utils.asm.util;

import dev.l3g7.griefer_utils.asm.mappings.MappingNode;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ASMUtil extends InsnListUtil implements Opcodes {

	public static MethodNode currentMethodNode = null;

	public static void updateMethod(MethodNode node) {
		currentMethodNode = node;
		InsnUtil.labelStack.clear();
	}

	public static String getDescription(MappingNode.Method method) {
		return "(" + Arrays.stream(method.params).map(MappingNode.Class::asType).collect(Collectors.joining()) + ")" + method.returnType.asType();
	}

}
