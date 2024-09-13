/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings.types;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import net.labymod.settings.elements.ControlElement;

public class CategorySettingImpl extends ControlElement implements Laby3Setting<CategorySetting, Object>, CategorySetting {

	private final ExtendedStorage<Object> storage = new ExtendedStorage<>(e -> JsonNull.INSTANCE, e -> NULL, NULL);

	public CategorySettingImpl() {
		super("§cNo name set", null);
		setSettingEnabled(true);
		setHoverable(true);
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	@Override
	public ExtendedStorage<Object> getStorage() {
		return storage;
	}

}
