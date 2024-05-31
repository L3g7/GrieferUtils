/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.bridges;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.api.bridges.MinecraftBridge.McItemStack;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import net.labymod.api.client.world.item.ItemStack;

@Bridged
public interface ItemBridge {

	ItemBridge itemBridge = FileProvider.getBridge(ItemBridge.class);

	McItemStack getDefaultStack();

	ItemStack toLabyStack(McItemStack nbt);

	McItemStack fromLabyStack(ItemStack itemStack);

	JsonElement serialize(McItemStack itemStack);

	McItemStack deserialize(JsonElement nbt);


	boolean isConvertableToLabyStack(Object obj);

	ItemStack convertToLabyStack(Object obj);

}
