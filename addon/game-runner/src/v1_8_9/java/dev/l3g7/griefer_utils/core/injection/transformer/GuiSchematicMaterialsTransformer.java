/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.injection.transformer;

import dev.l3g7.griefer_utils.core.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.core.injection.transformer.Transformer.Target;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

@Target("com.github.lunatrius.schematica.client.gui.control.GuiSchematicMaterials")
public class GuiSchematicMaterialsTransformer extends Transformer {

	private static final MethodInsnNode OPEN_MATERIAL_FILE = new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/BetterSchematica", "openMaterialFile", "()V", false);
	private static final MethodInsnNode WRITE_ERROR_MESSAGE = new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/BetterSchematica", "writeErrorMessage", "()V", false);

	@Override
	protected void process() {
		MethodNode method = getMethod("dumpMaterialList", "(Ljava/util/List;)V");
		getIterator(method, INVOKESTATIC, "write").add(OPEN_MATERIAL_FILE);
		getIterator(method, INVOKEINTERFACE, "error").add(WRITE_ERROR_MESSAGE);
	}

}
