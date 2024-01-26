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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.settings.types.NumberSetting;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.util.math.MathHelper;

public class NumberSettingImpl extends AbstractSettingImpl<NumberSetting, Integer> implements NumberSetting {

	private Component placeholder = null;
	private int min, max = Integer.MAX_VALUE;

	public NumberSettingImpl() {
		super(JsonPrimitive::new, JsonElement::getAsInt, 0);
	}

	@Override
	protected Widget[] createWidgets() {
		TextFieldWidget widget = new TextFieldWidget();
		widget.placeholder(placeholder);

		widget.validator(v -> {
			try {
				Integer.parseInt(v);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		});
		widget.setText(String.valueOf(get()));
		widget.updateListener(v -> {
			try {
				set(MathHelper.clamp(Integer.parseInt(v), min, max)); // TODO update on outOfFocus, mark if invalid?
			} catch (NumberFormatException ignored) {}
		});
		widget.maximalLength(10);
		callback(v -> widget.setText(String.valueOf(v), true));

		return new Widget[]{widget};
	}

	@Override
	public NumberSetting placeholder(String placeholder) {
		this.placeholder = Component.text(placeholder);
		return this;
	}

	@Override
	public NumberSetting min(int min) {
		this.min = min;
		return this;
	}

	@Override
	public NumberSetting max(int max) {
		this.max = max;
		return this;
	}

}
