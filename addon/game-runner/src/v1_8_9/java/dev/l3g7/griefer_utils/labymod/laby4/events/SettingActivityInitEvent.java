/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.events;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.labymod.laby4.bridges.LabyBridgeImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingsImpl;
import net.labymod.api.client.gui.screen.activity.activities.labymod.child.SettingContentActivity;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.WrappedWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.ScrollWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.event.labymod.config.SettingWidgetInitializeEvent;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

@ExclusiveTo(LABY_4)
public class SettingActivityInitEvent extends Event { // NOTE: use SettingWidgetInitializeEvent?

	public final SettingContentActivity activity;
	public final FlexibleContentWidget container;

	private SettingActivityInitEvent(SettingContentActivity activity, FlexibleContentWidget container) {
		this.activity = activity;
		this.container = container;
	}

	public <T extends Widget> T get(String... idPath) {
		Widget widget = container;
		for (String id : idPath) {
			Widget child = ((AbstractWidget<?>) widget).getChild(id);
			if (child == null)
				return null;

			//noinspection deprecation
			widget = ((WrappedWidget) child).childWidget();
		}
		return c(widget);
	}

	public VerticalListWidget<Widget> settings() {
		ScrollWidget widget = get("scroll");
		return c(widget.contentWidget());
	}

	public Setting holder() {
		return activity.getCurrentHolder();
	}

	@OnEnable
	public static void register() {
		LabyBridgeImpl.register(SettingWidgetInitializeEvent.class, event -> {
			SettingContentActivity activity = (SettingContentActivity) event.parentScreen().currentScreen().unwrap();

			// Intercept children#add call at end of initialization
			SettingsImpl.hookChildAdd(activity.document(), e -> new SettingActivityInitEvent(activity, c(e)).fire());
		});
	}

}
