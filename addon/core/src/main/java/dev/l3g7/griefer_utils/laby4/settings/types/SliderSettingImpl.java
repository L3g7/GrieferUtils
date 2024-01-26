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
import dev.l3g7.griefer_utils.settings.types.SliderSetting;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget;

public class SliderSettingImpl extends AbstractSettingImpl<SliderSetting, Integer> implements SliderSetting {

	private int min, max = Integer.MAX_VALUE;

	public SliderSettingImpl() {
		super(JsonPrimitive::new, JsonElement::getAsInt, 0);
	}

	@Override
	protected Widget[] createWidgets() {
		SliderWidget widget = new SliderWidget(1, v -> set((int) v));
		widget.range(min, max);
		widget.setValue((double) get());
		callback(widget::setValue);

		return new Widget[]{widget};
	}

	@Override
	public SliderSetting min(int min) {
		this.min = min;
		return this;
	}

	@Override
	public SliderSetting max(int max) {
		this.max = max;
		return this;
	}

}
