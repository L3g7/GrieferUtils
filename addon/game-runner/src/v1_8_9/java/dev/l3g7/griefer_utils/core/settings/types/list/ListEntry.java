/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.types.list;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ListEntry<E extends ListEntry<E>> extends Named {

	E createNew();

	default String resourceIcon() {return null;}
	default ItemStack itemIcon() {return null;}

	List<BaseSetting<?>> toSettings();

	void load(JsonElement data);

	JsonElement encode();

}
