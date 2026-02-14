/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

class ItemDumper {

	public static final KeySetting key = KeySetting.create()
		.name("Item-Dumper")
		.description("Dumpt das Item unter dem Mauscursor.")
		.icon("XZRF:gold_ingot")
		.triggersInContainers()
		.pressCallback(pressed -> {
			if (!pressed || !DebugSettings.enabled.get())
				return;

			if (!(mc().currentScreen instanceof GuiContainer gc))
				return;

			Slot theSlot = Reflection.get(gc, "theSlot");
			if (theSlot != null && theSlot.getHasStack())
				System.out.println(theSlot.getStack().writeToNBT(new NBTTagCompound()));
		});

}
