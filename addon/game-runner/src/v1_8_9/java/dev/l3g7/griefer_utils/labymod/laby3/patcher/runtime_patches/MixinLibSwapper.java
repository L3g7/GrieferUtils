/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.patcher.runtime_patches;

import dev.l3g7.griefer_utils.labymod.laby3.patcher.RuntimePatcher.Patcher;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Overwrites method descriptors in MixinPlugin to use the ClassNode class of the bundled library.
 */
public class MixinLibSwapper extends Patcher {

	@Override
	public boolean isCompatible(String target, boolean checkForwardCompatibility) {
		return !checkForwardCompatibility && target.equals("dev.l3g7.griefer_utils.core.injection.MixinPlugin");
	}

	@Override
	public void patch(ClassNode classNode) {
		if (!classNode.name.equals("dev/l3g7/griefer_utils/core/injection/MixinPlugin"))
			return;

		for (MethodNode method : classNode.methods)
			method.desc = method.desc.replace("Lorg/objectweb/asm/tree/ClassNode;", "Lorg/spongepowered/asm/lib/tree/ClassNode;");

		setModified();
	}

}
