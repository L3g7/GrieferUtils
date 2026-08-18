/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.impl.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.SwitchSettingImpl;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@Bridge.ExclusiveTo(LABY_4)
public class CopyDisplaySettingLaby4 extends SwitchSettingImpl {

	public CopyDisplaySettingLaby4() {
		EventRegisterer.register(this);
	}

	/**
	 * Replaces the advanced-button icon with pencil_vec.
	 */
	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != parent)
			return;

		for (Widget w : event.settings().getChildren()) {
			if (w instanceof SettingWidget s && s.setting() == this) {
				SettingsImpl.hookChildAdd(s, e -> {
					if (e.childWidget() instanceof FlexibleContentWidget content) {
						ButtonWidget btn = (ButtonWidget) content.getChild("advanced-button").childWidget();
						btn.updateIcon(Icons.of("high_res/pencil_vec"));
					}
				});
				break;
			}
		}

	}

}
