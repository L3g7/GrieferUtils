package dev.l3g7.griefer_utils.features.uncategorized.scripts;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.lang.invoke.MethodHandles;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

class Util {

	public static String removeTrailingComma(String str) {
		return str.endsWith(",") ? str.substring(0, str.length() - 1) : str;
	}

	public static String resolveClass(String name) {
		Class<?> result = tryLoad(name);

		if (result == null)
			result = tryLoad("java.lang." + name);

		if (result == null)
			return name;

		return result.getName().replace('.', '/');
	}

	private static Class<?> tryLoad(String name) {
		try {
			return Class.forName(name.replace('/', '.'));
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static Class<?> loadClass(ClassNode node) {
		ClassWriter writer = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
		node.accept(writer);

		byte[] classBytes = writer.toByteArray();

		try {
			return MethodHandles.privateLookupIn(Scripts.class, MethodHandles.lookup()).defineClass(classBytes);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static AbstractInsnNode getNumberInsn(int num) {
		if (num == -1)
			return new InsnNode(ICONST_M1);
		if (num >= 0 && num <= 5)
			return new InsnNode(ICONST_0 + num);

		if ((byte) num == num)
			return new IntInsnNode(BIPUSH, num);
		if ((short) num == num)
			return new IntInsnNode(SIPUSH, num);

		return new LdcInsnNode(num);
	}

}
