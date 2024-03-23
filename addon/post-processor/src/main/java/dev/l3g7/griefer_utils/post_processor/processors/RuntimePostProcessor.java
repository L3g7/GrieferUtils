package dev.l3g7.griefer_utils.post_processor.processors;

import net.minecraft.launchwrapper.IClassTransformer;

/**
 * A processor applied at runtime.
 */
public abstract class RuntimePostProcessor implements IClassTransformer {

	public abstract byte[] transform(String fileName, byte[] basicClass);

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		return transform(transformedName.replace('.', '/').concat(".class"), basicClass);
	}

}
