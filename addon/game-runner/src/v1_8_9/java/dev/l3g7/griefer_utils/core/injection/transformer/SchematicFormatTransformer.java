/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.injection.transformer;

import dev.l3g7.griefer_utils.core.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.core.injection.transformer.Transformer.Target;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ListIterator;

@Target("com.github.lunatrius.schematica.world.schematic.SchematicFormat")
public class SchematicFormatTransformer extends Transformer {

	@Override
	protected void process() {
		MethodNode methodNode = getMethod("readFromFile", "(Ljava/io/File;)Lcom/github/lunatrius/schematica/api/ISchematic;");
		ListIterator<AbstractInsnNode> iterator = getIterator(methodNode, ASTORE, n -> true);
		iterator.next();
		iterator.add(new VarInsnNode(ALOAD, 1));
		iterator.add(new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/SaveSchematicaPosition", "readFromNBT", "(Lnet/minecraft/nbt/NBTTagCompound;)V", false));
	}

}
