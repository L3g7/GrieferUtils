package dev.l3g7.griefer_utils.injection;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.HashMap;
import java.util.Map;

public class Injector implements IClassTransformer {

	private static final Map<String, Transformer> transformers = new HashMap<>();

	public Injector() {
		// Initialize Mixin
		MixinBootstrap.init();
		Mixins.addConfiguration("griefer_utils.mixins.json");
		MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);

		// Load transformers
		for (ClassMeta meta : FileProvider.getClassesWithSuperClass(Transformer.class)) {
			Transformer transformer = Reflection.construct(meta.load());
			transformers.put(transformer.getTarget(), transformer);
		}
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (name.startsWith("com.github.lunatrius.schematica"))
			Constants.SCHEMATICA = true;

		Transformer transformer = transformers.get(transformedName);
		if (transformer != null) {
			ClassNode classNode = new ClassNode();
			ClassReader reader = new ClassReader(basicClass);
			reader.accept(classNode, 0);

			transformer.transform(classNode);

			ClassWriter writer = new ClassWriter(3);
			classNode.accept(writer);
			return writer.toByteArray();
		}

		return basicClass;
	}

}
