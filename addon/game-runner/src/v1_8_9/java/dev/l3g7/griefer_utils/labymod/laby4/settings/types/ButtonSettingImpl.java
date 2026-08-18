/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;

public class ButtonSettingImpl extends AbstractSettingImpl<ButtonSetting, Object> implements ButtonSetting {

	private Icon buttonIcon;
	private String buttonLabel;

	public ButtonSettingImpl() {
		super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
	}

	@Override
	protected Widget[] createWidgets() {
		Component component = buttonLabel == null ? null : Component.text(buttonLabel);
		ButtonWidget widget = ButtonWidget.component(component, buttonIcon, () -> set(null))
			.addId("mods-setting-advanced-button"); // Fix for button size

		// ModsSettingWidget resets the widget and its icon, which causes it to disappear.
		// The component is not synced to the text property (bug?), so that stays.
		widget.icon().updateDefaultValue(widget.icon().get());

		return new Widget[]{
			widget
		};
	}

	@Override
	public ButtonSetting buttonIcon(String icon) {
		return buttonIcon(Icons.of(icon));
	}

	public ButtonSetting buttonIcon(Icon icon) {
		buttonIcon = icon;
		return this;
	}

	@Override
	public ButtonSetting buttonLabel(String label) {
		buttonLabel = label;
		return this;
	}

}
