/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.settings.types;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.settings.types.CategorySetting;
import net.labymod.api.client.gui.screen.widget.Widget;

public class CategorySettingImpl extends AbstractSettingImpl<CategorySetting, Object> implements CategorySetting {

	protected boolean enabled = true;

	public CategorySettingImpl() {
		super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
	}

	@Override
	protected Widget[] createWidgets() {
		return null;
	}

}
