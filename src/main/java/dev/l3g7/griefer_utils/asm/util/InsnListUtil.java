package dev.l3g7.griefer_utils.asm.util;

import dev.l3g7.griefer_utils.asm.mappings.MappingNode;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Arrays;
import java.util.stream.Stream;

public class InsnListUtil extends InsnUtil implements Opcodes {

	public static final String INJECT_END = "<injectEnd>";

	private static boolean equals(MappingNode mappingNode, AbstractInsnNode checkNode) {
		if(checkNode instanceof FieldInsnNode && mappingNode instanceof MappingNode.Field) {
			FieldInsnNode fieldInsnNode = (FieldInsnNode) checkNode;
			MappingNode.Field field = (MappingNode.Field) mappingNode;
			return fieldInsnNode.name.equals(field.toString())
					&& fieldInsnNode.desc.equals(field.type.asType())
					&& fieldInsnNode.owner.equals(field.owner.toString());
		} else if(checkNode instanceof MethodInsnNode && mappingNode instanceof MappingNode.Method) {
			MethodInsnNode methodInsnNode = (MethodInsnNode) checkNode;
			MappingNode.Method method = (MappingNode.Method) mappingNode;
			return methodInsnNode.name.equals(method.toString())
					&& methodInsnNode.desc.equals(ASMUtil.getDescription(method));
		}
		return false;
	}

	private static InsnList insnListByArray(AbstractInsnNode[] nodes) {
		InsnList list = new InsnList();
		Arrays.stream(nodes).forEach(list::add);
		return list;
	}

	public static void insertAtStart(AbstractInsnNode... nodes) {
		InsnList insnList = ASMUtil.currentMethodNode.instructions;
		InsnList nodeList = insnListByArray(nodes);
		nodeList.add(InsnUtil.label(ASMUtil.INJECT_END));
		insnList.insert(nodeList);
	}

	public static void insertBeforeReturns(AbstractInsnNode... nodes) {
		InsnList insnList = ASMUtil.currentMethodNode.instructions;
		stream(insnList)
				.filter(n -> n.getOpcode() == ARETURN || n.getOpcode() == RETURN)
				.forEach(n -> insnList.insertBefore(n, insnListByArray(nodes)));
	}

	public static InsnListChunk findByMappings(MappingNode... nodes) {
		insnLoop:
		for(int i = 0; i < ASMUtil.currentMethodNode.instructions.size(); i++) {
			AbstractInsnNode checkNode = ASMUtil.currentMethodNode.instructions.get(i);

			// Check if nodes (and following nodes) match
			for(MappingNode mappingNode : nodes) {
				if(equals(mappingNode, checkNode))
					checkNode = checkNode.getNext();
				else continue insnLoop;
			}

			// If this point is reached, all mappings match
			return new InsnListChunk(ASMUtil.currentMethodNode.instructions, i, i + nodes.length - 1);
		}
		throw new IllegalStateException("Could not find mappings!");
	}

	public static void insertAfter(AbstractInsnNode target, AbstractInsnNode... nodes) {
		ASMUtil.currentMethodNode.instructions.insert(target, insnListByArray(nodes));
	}

	public static void replace(AbstractInsnNode target, AbstractInsnNode value) {
		InsnList insnList = ASMUtil.currentMethodNode.instructions;
		insnList.insert(target, value);
		insnList.remove(target);
	}

	public static Stream<AbstractInsnNode> stream(InsnList list) {
		return Arrays.stream(list.toArray());
	}


	public static class InsnListChunk {

		private final InsnList list;
		private final int start, end;

		private InsnListChunk(InsnList list, int start, int end) {
			this.list = list;
			this.start = start;
			this.end = end;
		}

		public AbstractInsnNode getPrevious() {
			return list.get(start).getPrevious();
		}

		public AbstractInsnNode getFollowing() {
			return list.get(end).getNext();
		}

	}
}
