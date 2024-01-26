/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.laby4.settings.types;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.renderer.EntryRenderer;
import net.labymod.api.client.render.font.RenderableComponent;
import org.jetbrains.annotations.NotNull;

public class DropDownSettingImpl<E extends Enum<E> & Named> extends AbstractSettingImpl<DropDownSetting<E>, E> implements DropDownSetting<E> {

	private final Class<E> enumClass;

	public DropDownSettingImpl(Class<E> enumClass) {
		super(e -> new JsonPrimitive(e.name()), s -> Enum.valueOf(enumClass, s.getAsString()), enumClass.getEnumConstants()[0]);
		this.enumClass = enumClass;
	}

	@Override
	protected Widget[] createWidgets() {
		DropdownWidget<E> widget = new DropdownWidget<>();
		widget.setSelected(get());
		widget.setChangeListener(this::set);

		widget.setEntryRenderer(new EntryRenderer<E>() {

			@Override
			public float getWidth(E entry, float maxWidth) {
				return this.toRenderableComponent(entry, maxWidth).getWidth();
			}

			@Override
			public float getHeight(E entry, float maxWidth) {
				return this.toRenderableComponent(entry, maxWidth).getHeight();
			}

			@Override
			public @NotNull Widget createEntryWidget(E entry) {
				return ComponentWidget.component(this.toComponent(entry));
			}

			private Component toComponent(E entry) {
				return Component.text(entry.getName());
			}

			private RenderableComponent toRenderableComponent(E entry, float maxWidth) {
				return RenderableComponent.builder()
					.maxWidth(maxWidth)
					.disableCache()
					.format(this.toComponent(entry));
			}

		});

		widget.addAll(enumClass.getEnumConstants());

		callback(widget::setSelected);

		return new Widget[]{widget};
	}

}
