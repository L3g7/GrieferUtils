/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

package dev.l3g7.griefer_utils.features.uncategorized;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.GuiGrieferInfo;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;

@Singleton
public class GrieferInfo extends Feature {

	private final KeySetting setting = new KeySetting()
		.name("Taste")
		.icon("key")
		.description("Mit welcher Taste das Gui geöffnet werden soll.")
		.pressCallback(b -> { if (b) GuiGrieferInfo.GUI.open(); });

	@MainElement
	private final CategorySetting button = new CategorySetting()
		.name("§xGriefer.Info")
		.icon("griefer_info")
		.subSettings(setting, new HeaderSetting(), new HeaderSetting("Das Griefer.Info Gui lässt sich auch mit /info oder /gi öffnen."));

}
