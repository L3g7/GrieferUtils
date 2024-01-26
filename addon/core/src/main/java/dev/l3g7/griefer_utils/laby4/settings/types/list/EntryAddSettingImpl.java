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

package dev.l3g7.griefer_utils.laby4.settings.types.list;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.settings.types.list.EntryAddSetting;
import net.labymod.api.client.gui.screen.widget.Widget;

public class EntryAddSettingImpl extends AbstractSettingImpl<EntryAddSetting, Object> implements EntryAddSetting {

	public EntryAddSettingImpl() {
		super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
		icon("labymod_3/addons");
	}

	@Override
	protected Widget[] createWidgets() {
		return new Widget[]{}; // TODO: impl EntryAddSetting
	}

}
