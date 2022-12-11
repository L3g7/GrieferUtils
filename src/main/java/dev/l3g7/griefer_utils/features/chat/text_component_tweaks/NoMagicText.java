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

package dev.l3g7.griefer_utils.features.chat.text_component_tweaks;

import dev.l3g7.griefer_utils.event.events.network.TabListEvent;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.Material;
import net.minecraft.util.IChatComponent;

@Singleton
public class NoMagicText extends TextComponentTweak {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Magischen Text deaktivieren")
		.description("§r§fDeaktiviert den magischen / verschleierten / verschlüsselten Stil in Chatnachrichten.")
		.icon(Material.BLAZE_POWDER)
		.defaultValue(true)
		.callback(c -> TabListEvent.updatePlayerInfoList())
		.subSettings(chat, tab, item);

	@Override
	void modify(IChatComponent component) {
		component.getChatStyle().setObfuscated(false);
		component.getSiblings().forEach(this::modify);
	}

	@Override
	String modify(String message) {
		return message.replace("§k", "");
	}

}
