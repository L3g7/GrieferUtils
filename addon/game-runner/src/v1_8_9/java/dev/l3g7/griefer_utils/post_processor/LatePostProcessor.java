/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor;

import dev.l3g7.griefer_utils.post_processor.processors.*;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;
import java.util.List;

/**
 * A collection of transformers allowing the use of GrieferUtils created using the LabyMod 4 SDK in
 * LabyMod 3 by post-processing classes if loaded in LabyMod 3.
 */
public class LatePostProcessor implements IClassTransformer {

	private static final List<Processor> processors = Arrays.asList(
		new StringConcatShim(),
		new SwitchShim(),
		new AccessElevator(),
		new MixinLibSwapper(),
		new SuperclassRemapper()
	);

	@Override
	public byte[] transform(String name, String transformedName, byte[] classBytes) {
		String fileName = transformedName.replace('.', '/').concat(".class");
		if (!fileName.startsWith("dev/l3g7/griefer_utils/"))
			return classBytes;

		ClassNode classNode = new ClassNode();
		ClassReader reader = new ClassReader(classBytes);
		reader.accept(classNode, 0);

		boolean modified = false;
		for (Processor processor : processors) {
			processor.process(classNode);
			modified |= processor.modified;
			processor.reset();
		}

		if (!modified)
			return classBytes;

		ClassWriter writer = new BoundClassWriter();
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public abstract static class Processor {

		private boolean modified = false;

		public abstract void process(ClassNode classNode);

		private void reset() {
			modified = false;
		}

		protected void setModified() {
			modified = true;
		}

	}

	/**
	 * A ClassWriter loaded by the same ClassLoader as the processor. As {@link ClassWriter} is normally
	 * loaded by the parent ClassLoader of the one loading this addon, it wouldn't find the classes
	 * defined by its child ClassLoader and getCommonSuperClass calls would fail.
	 */
	protected static class BoundClassWriter extends ClassWriter {
		public BoundClassWriter() {
			super(COMPUTE_MAXS | COMPUTE_FRAMES);
		}
	}

}
