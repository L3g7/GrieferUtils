/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.injection.transformer;

import dev.l3g7.griefer_utils.core.injection.transformer.Transformer.Target;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

@Target("com.github.lunatrius.schematica.client.gui.load.GuiSchematicLoad")
public class GuiSchematicLoadTransformer extends Transformer {

	private static final MethodInsnNode SET_POSITION_AFTER_LOADING = new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/SaveSchematicaPosition", "setPositionAfterLoading", "(Lcom/github/lunatrius/schematica/client/world/SchematicWorld;)V", false);

	@Override
	protected void process() {
		MethodNode methodNode = getMethod("loadSchematic", "()V");
		getIterator(methodNode, INVOKESTATIC, "moveSchematicToPlayer").set(SET_POSITION_AFTER_LOADING);
	}

}
