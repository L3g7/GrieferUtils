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
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ListIterator;

@Target("com.github.lunatrius.schematica.proxy.ClientProxy")
public class ClientProxyTransformer extends Transformer {

	@Override
	protected void process() {
		MethodNode methodNode = getMethod("loadSchematic", "(Lnet/minecraft/entity/player/EntityPlayer;Ljava/io/File;Ljava/lang/String;)Z");
		ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
		while (iterator.hasNext())
			iterator.next();

		while (iterator.hasPrevious()) {
			AbstractInsnNode insn = iterator.previous();
			if (insn.getOpcode() != ICONST_1)
				continue;

			iterator.add(new VarInsnNode(ALOAD, 2));
			iterator.add(new VarInsnNode(ALOAD, 3));
			iterator.add(new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/SaveSchematicaPosition", "onSchematicLoaded", "(Ljava/io/File;Ljava/lang/String;)V", false));
		}
	}

}
