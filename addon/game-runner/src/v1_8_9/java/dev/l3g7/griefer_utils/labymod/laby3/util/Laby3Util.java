/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.util;

import net.labymod.utils.texture.DynamicModTexture;
import net.minecraft.util.ResourceLocation;

public class Laby3Util { // FIXME: dissolve; moving these methods to their usage results in crashes due to wrong mappings

	public static DynamicModTexture createDynamicTexture(String path, String url) {
		return new DynamicModTexture(new ResourceLocation(path), url);
	}

}
