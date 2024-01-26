/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.settings.types.list;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.settings.types.list.EntryAddSetting;
import net.labymod.api.client.gui.screen.widget.Widget;

public class EntryAddSettingImpl extends AbstractSettingImpl<EntryAddSetting, Object> implements EntryAddSetting {

	public EntryAddSettingImpl() {
		super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
		icon("labymod_3/addons");
	}

	@Override
	protected Widget[] createWidgets() {
		return new Widget[]{}; // TODO: impl EntryAddSetting
	}

}
