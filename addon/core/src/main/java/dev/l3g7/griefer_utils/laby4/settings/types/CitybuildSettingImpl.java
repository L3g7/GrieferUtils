/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.settings.types;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.misc.Citybuild;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.settings.types.CitybuildSetting;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.renderer.EntryRenderer;
import net.labymod.api.client.render.font.RenderableComponent;
import org.jetbrains.annotations.NotNull;

import static dev.l3g7.griefer_utils.api.misc.Citybuild.CitybuildIconBridge.citybuildIconBridge;
import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;

public class CitybuildSettingImpl extends AbstractSettingImpl<CitybuildSetting, Citybuild> implements CitybuildSetting {

	public CitybuildSettingImpl() {
		super(e -> {
			String name = e.name();
			if (e == Citybuild.ANY) {
				name = "Egal";
			} else if (!e.name().startsWith("CB")) {
				StringBuilder sb = new StringBuilder(name.toLowerCase());
				sb.setCharAt(0, e.name().charAt(0));
				name = sb.toString();
			}

			return new JsonPrimitive(name);
		}, e -> Citybuild.getCitybuild(e.getAsString()), Citybuild.ANY);
	}

	@Override
	protected Widget[] createWidgets() {
		DropdownWidget<Citybuild> widget = new DropdownWidget<>();
		widget.setSelected(get());
		widget.setChangeListener(this::set);

		widget.setEntryRenderer(new EntryRenderer<>() {

			@Override
			public float getWidth(Citybuild entry, float maxWidth) {
				return toRenderableComponent(entry, maxWidth).getWidth();
			}

			@Override
			public float getHeight(Citybuild entry, float maxWidth) {
				return toRenderableComponent(entry, maxWidth).getHeight();
			}

			@Override
			public @NotNull Widget createEntryWidget(Citybuild entry) {
				return ComponentWidget.component(toComponent(entry));
			}

			private Component toComponent(Citybuild entry) {
				return Component.icon(c(citybuildIconBridge.createIcon(entry)))
					.append(Component.text(entry.getName()));
			}

			private RenderableComponent toRenderableComponent(Citybuild entry, float maxWidth) {
				return RenderableComponent.builder()
					.maxWidth(maxWidth)
					.disableCache()
					.format(toComponent(entry));
			}

		});

		widget.addAll(Citybuild.values());

		callback(v -> widget.setSelected(v, false));

		return new Widget[]{widget};
	}

}