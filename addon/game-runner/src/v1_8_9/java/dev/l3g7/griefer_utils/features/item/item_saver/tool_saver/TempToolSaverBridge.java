/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver.tool_saver;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import net.minecraft.item.ItemStack;

@Bridged
public interface TempToolSaverBridge {

	boolean isEnabled();

	boolean shouldCancel(ItemStack heldItem);

}
