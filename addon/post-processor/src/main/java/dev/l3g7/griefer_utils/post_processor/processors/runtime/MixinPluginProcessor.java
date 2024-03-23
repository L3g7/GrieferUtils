package dev.l3g7.griefer_utils.post_processor.processors.runtime;

import dev.l3g7.griefer_utils.post_processor.processors.RuntimePostProcessor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Overwrites method descriptors in MixinPlugin to use the bundled ClassNode class.
 */
public class MixinPluginProcessor extends RuntimePostProcessor {

	public static final MixinPluginProcessor INSTANCE = new MixinPluginProcessor();

	private MixinPluginProcessor() {}

	@Override
	public byte[] transform(String fileName, byte[] basicClass) {
		if (!fileName.equals("dev/l3g7/griefer_utils/injection/MixinPlugin.class"))
			return basicClass;

		ClassNode classNode = new ClassNode();
		ClassReader reader = new ClassReader(basicClass);
		reader.accept(classNode, 0);

		for (MethodNode method : classNode.methods)
			method.desc = method.desc.replace("Lorg/objectweb/asm/tree/ClassNode;", "Lorg/spongepowered/asm/lib/tree/ClassNode;");

		ClassWriter writer = new ClassWriter(3);
		classNode.accept(writer);
		return writer.toByteArray();
	}

}
