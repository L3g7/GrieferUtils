package dev.l3g7.griefer_utils.injection.transformer;

import dev.l3g7.griefer_utils.util.misc.Mapping;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static dev.l3g7.griefer_utils.util.misc.Mapping.MappingTarget.NOTCH;

/**
 * A Bytecode transformer.
 */
public abstract class Transformer implements Opcodes {

	protected ClassNode classNode;
	protected String transformedName;

	public void transform(ClassNode node, String transformedName) {
		this.classNode = node;
		this.transformedName = transformedName;
		process();
	}

	protected abstract void process();

	protected MethodNode getMethod(String name, String desc) {
		String mappedName = Mapping.mapMethodName(NOTCH, transformedName, name, desc);
		String mappedDesc = Mapping.mapMethodDesc(NOTCH, desc);
		return classNode.methods.stream()
			.filter(m -> m.name.equals(mappedName) && m.desc.equals(mappedDesc))
			.findFirst()
			.orElseThrow(() -> new NoSuchMethodError("Could not find " + name + desc + " / " + mappedName + mappedDesc + "!"));
	}

	protected boolean matches(AbstractInsnNode node, int opcode, String... args) {
		if (node.getOpcode() != opcode)
			return false;

		switch (opcode) {
			case GETSTATIC: {
				assert args.length == 3;
				FieldInsnNode insn = (FieldInsnNode) node;
				return
					insn.owner.equals(Type.getType(Mapping.mapClass(NOTCH, Type.getObjectType(args[0]).getDescriptor())).getInternalName()) &&
					insn.name.equals(Mapping.mapField(NOTCH, args[0], args[1])) &&
					insn.desc.equals(Mapping.mapClass(NOTCH, args[2]));
			}
			case INVOKEVIRTUAL: {
				MethodInsnNode insn = (MethodInsnNode) node;
				System.out.println(
					insn.owner + "=" + Type.getType(Mapping.mapClass(NOTCH, Type.getObjectType(args[0]).getDescriptor())).getInternalName() +" "
						+ insn.name + "=" + Mapping.mapMethodName(NOTCH, args[0], args[1], args[2]) + " "
						+ insn.desc + "="  + Mapping.mapMethodDesc(NOTCH, args[2]));
				return
					insn.owner.equals(Type.getType(Mapping.mapClass(NOTCH, Type.getObjectType(args[0]).getDescriptor())).getInternalName()) &&
					insn.name.equals(Mapping.mapMethodName(NOTCH, args[0], args[1], args[2])) &&
					insn.desc.equals(Mapping.mapMethodDesc(NOTCH, args[2]));
			}
			default:
				throw new UnsupportedOperationException("matches for " + opcode + " not implemented!");
		}
	}

}
