/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.bsf.data;

import dev.l3g7.griefer_utils.core.misc.gui.guis.GuiBigChest;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.awt.*;

public interface BSFSearchable {

	BSFName getName();

	GuiBigChest.TextureItem getIcon();

	Color getColor();

	static ItemStack toStack(Object object) {
		if (object == null)
			return null;

		if (object instanceof ItemStack is)
			return is;

		if (object instanceof Item i)
			return new ItemStack(i);

		return new ItemStack((Block) object);
	}

}
