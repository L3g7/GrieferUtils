package dev.l3g7.griefer_utils.asm;

import net.minecraft.launchwrapper.IClassTransformer;

/**
 * description missing.
 */
public class Transformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		return basicClass;
	}

}
