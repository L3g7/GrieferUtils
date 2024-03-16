/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby3;

import net.minecraft.launchwrapper.IClassTransformer;

public class PreStart implements IClassTransformer {

	public PreStart()  {
		if (System.setProperty("griefer_utils_load_flag", "") != null)
			throw new Error("GrieferUtils wurde bereits geladen!");

		// TODO AutoUpdater.update()
		EarlyStart.start();
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		return basicClass;
	}

}
