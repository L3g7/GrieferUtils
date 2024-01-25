/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.injection.transformer.transformers;

import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.injection.transformer.Transformer.Target;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

@Target("com.github.lunatrius.schematica.client.renderer.chunk.overlay.RenderOverlay")
public class RenderOverlayTransformer extends Transformer {

	@Override
	protected void process() {
		MethodNode methodNode = getMethod("func_178581_b", "(FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V");
		ListIterator<AbstractInsnNode> iterator = getIterator(methodNode, INVOKESTATIC, "drawCuboid");
		iterator.previous();
		MethodInsnNode min = (MethodInsnNode) iterator.next();

		iterator.set(new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/HighlightSchematicaBlocks", "drawCuboid", min.desc, false));
	}

}
