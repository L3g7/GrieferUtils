/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.settings.types;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.settings.types.ButtonSetting;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;

public class ButtonSettingImpl extends AbstractSettingImpl<ButtonSetting, Object> implements ButtonSetting {

	private Icon buttonIcon;
	private String buttonLabel = "§cNo icon";

	public ButtonSettingImpl() {
		super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
		EventRegisterer.register(this);
	}

	@Override
	protected Widget[] createWidgets() {
		return null;
	}

	@Override
	public ButtonSetting buttonIcon(Object icon) {
		buttonIcon = SettingsImpl.buildIcon(icon);
		return this;
	}

	@Override
	public ButtonSetting buttonLabel(String label) {
		buttonLabel = label;
		return this;
	}

	@Override
	public boolean hasAdvancedButton() {
		return true;
	}

	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != parent)
			return;

		for (Widget w : event.settings().getChildren()) {
			if (w instanceof SettingWidget s && s.setting() == this) {
				SettingsImpl.hookChildAdd(s, e -> {
					if (e.childWidget() instanceof FlexibleContentWidget content) {
						ButtonWidget btn = buttonIcon == null ?
							ButtonWidget.component(Component.text(buttonLabel), null, () -> set(null)) :
							ButtonWidget.icon(buttonIcon, () -> set(null));

						if (buttonIcon != null)
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
