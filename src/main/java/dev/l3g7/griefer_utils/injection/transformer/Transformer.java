package dev.l3g7.griefer_utils.injection.transformer;

import dev.l3g7.griefer_utils.util.misc.Mapping;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Objects;

import static dev.l3g7.griefer_utils.util.misc.Mapping.MappingTarget.NOTCH;

/**
 * A Bytecode transformer.
 */
public abstract class Transformer implements Opcodes {

	protected ClassNode classNode;
	protected final String target;

	protected Transformer() {
		this.target = getClass().getDeclaredAnnotation(Target.class).value();
	}

	public void transform(ClassNode node) {
		this.classNode = node;
		process();
	}

	protected abstract void process();

	protected MethodNode getMethod(String name, String desc) {
		String mappedName = Mapping.mapMethodName(NOTCH, target, name, desc);
		String mappedDesc = Mapping.mapMethodDesc(NOTCH, desc);
		return classNode.methods.stream()
			.filter(m -> m.name.equals(mappedName) && m.desc.equals(mappedDesc))
			.findFirst()
			.orElseThrow(() -> new NoSuchMethodError("Could not find " + name + desc + " / " + mappedName + mappedDesc + "!"));
	}

	protected boolean matches(AbstractInsnNode node, int opcode, Object... args) {
		if (node.getOpcode() != opcode)
			return false;

		switch (opcode) {
			case LDC: {
				assert args.length == 1;
				LdcInsnNode insn = (LdcInsnNode) node;
				return Objects.equals(insn.cst, args[0]);
			}
			case GETSTATIC: {
				assert args.length == 3;
				FieldInsnNode insn = (FieldInsnNode) node;
				return
					insn.owner.equals(Type.getType(Mapping.mapClass(NOTCH, Type.getObjectType((String) args[0]).getDescriptor())).getInternalName()) &&
					insn.name.equals(Mapping.mapField(NOTCH, (String) args[0], (String) args[1])) &&
					insn.desc.equals(Mapping.mapClass(NOTCH, (String) args[2]));
			}
			case INVOKEINTERFACE:
			case INVOKESTATIC:
			case INVOKEVIRTUAL: {
				assert args.length == 3;
				MethodInsnNode insn = (MethodInsnNode) node;
				return
					insn.owner.equals(Type.getType(Mapping.mapClass(NOTCH, Type.getObjectType((String) args[0]).getDescriptor())).getInternalName()) &&
					insn.name.equals(Mapping.mapMethodName(NOTCH, (String) args[0], (String) args[1], (String) args[2])) &&
					insn.desc.equals(Mapping.mapMethodDesc(NOTCH, (String) args[2]));
			}
			default:
				throw new UnsupportedOperationException("matches for " + opcode + " not implemented!");
		}
	}

	public String getTarget() {
		return target;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Target {
		String value();
	}
}