/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.core.settings.types.list.EntryAddSetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.AbstractSettingImpl;
import net.labymod.api.Textures;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;

public class EntryAddSettingImpl extends AbstractSettingImpl<EntryAddSetting, Object> implements EntryAddSetting {

	public EntryAddSettingImpl() {
		super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
	}

	@Override
	protected Widget[] createWidgets() {
		ButtonWidget widget = ButtonWidget.component(null, Textures.SpriteCommon.SMALL_ADD_WITH_SHADOW, () -> set(null))
			.addId("mods-setting-advanced-button"); // Fix for button size

		// ModsSettingWidget resets the widget and its icon, which causes it to disappear.
		// The component is not synced to the text property (bug?), so that stays.
		widget.icon().updateDefaultValue(widget.icon().get());

		return new Widget[]{
			widget
		};
	}

}
