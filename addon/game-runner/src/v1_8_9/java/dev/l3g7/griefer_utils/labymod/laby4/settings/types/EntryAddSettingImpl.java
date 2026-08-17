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
import dev.l3g7.griefer_utils.core.settings.types.list.EntryAddSetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.ActivityInitializeEvent.SettingActivityInitEvent;
import net.labymod.api.Textures;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;

public class EntryAddSettingImpl extends AbstractSettingImpl<EntryAddSetting, Object> implements EntryAddSetting {

	private ButtonWidget widget;

	public EntryAddSettingImpl() {
		super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
		EventRegisterer.register(this);
	}

	@Override
	protected Widget[] createWidgets() {
		widget = ButtonWidget.component(Component.text(""), () -> set(null)).addId("grieferutils-fix-width");
		reinitWidget();
		return new Widget[]{widget};
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

		widget.updateIcon(Textures.SpriteCommon.SMALL_ADD_WITH_SHADOW);
	}

}
