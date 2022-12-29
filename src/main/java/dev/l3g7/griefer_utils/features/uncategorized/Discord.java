/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.features.uncategorized;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import net.labymod.settings.elements.ControlElement;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Singleton
public class Discord extends Feature {

	@MainElement
	private final SmallButtonSetting button = new SmallButtonSetting()
		.name("§zDiscord")
		.icon("discord")
		.buttonIcon(new ControlElement.IconData("griefer_utils/icons/discord_clyde.png"))
		.callback(() -> {
			try {
				Desktop.getDesktop().browse(new URI("https://grieferutils.l3g7.dev/discord"));
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException(e);
			}
		});

}