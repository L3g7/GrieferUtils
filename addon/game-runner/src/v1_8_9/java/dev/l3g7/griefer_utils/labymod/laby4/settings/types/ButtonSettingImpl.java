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
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.ActivityInitializeEvent.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;

public class ButtonSettingImpl extends AbstractSettingImpl<ButtonSetting, Object> implements ButtonSetting {

	private Icon buttonIcon;
	private String buttonLabel;
	private ButtonWidget widget;

	public ButtonSettingImpl() {
		super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
		EventRegisterer.register(this);
	}

	@Override
	protected Widget[] createWidgets() {
		widget = ButtonWidget.component(Component.text(""), () -> set(null)).addId("grieferutils-fix-width");
		reinitWidget();
		return new Widget[]{widget};
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

	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.parent() != parent)
			return;

		event.getActivity().addStyle("griefer_utils", "button-injection.lss");
		reinitWidget();
	}

	private void reinitWidget() {
		if (widget == null)
			return;

		if (buttonIcon != null)
			widget.updateIcon(buttonIcon);
		if (buttonLabel != null)
			widget.updateComponent(Component.text(buttonLabel));
	}
}
