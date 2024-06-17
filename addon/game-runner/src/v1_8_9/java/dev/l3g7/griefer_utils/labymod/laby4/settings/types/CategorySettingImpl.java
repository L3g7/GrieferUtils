/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.labymod.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;

public class CategorySettingImpl extends AbstractSettingImpl<CategorySetting, Object> implements CategorySetting {

	protected boolean enabled = true;

	public CategorySettingImpl() {
		super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
		EventRegisterer.register(this);
	}

	@Override
	protected Widget[] createWidgets() {
		return null;
	}

	@Override
	public boolean hasAdvancedButton() {
		return true;
	}

	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != parent)
			return;

		if (event.get("scroll") == null)
			return;

		for (Widget w : event.settings().getChildren()) {
			if (w instanceof SettingWidget s && s.setting() == this) {
				SettingsImpl.hookChildAdd(s, e -> {
					if (e.childWidget() instanceof FlexibleContentWidget content && getElements().isEmpty()) {
						ButtonWidget btn = ButtonWidget.component(null);
						btn.setVisible(false);

						btn.addId("advanced-button"); // required so LSS is applied
						content.removeChild("advanced-button");
						content.addContent(btn);
					}
				});
				break;
			}
		}

	}

}
