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

package dev.l3g7.griefer_utils.v1_8_9.features.chat;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.Priority;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.StringSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Feature;
import net.minecraft.init.Items;
import net.minecraft.util.ChatComponentText;

@Singleton
public class RealMoney extends Feature {

	private final StringSetting tag = StringSetting.create()
		.name("Tag")
		.description("Womit eingehenden Bezahlungen markiert werden sollen.")
		.icon(Items.name_tag)
		.defaultValue("&a [✔]");

	private final DropDownSetting<TagPosition> position = DropDownSetting.create(TagPosition.class)
		.name("Position")
		.description("Ob der Tag an den Anfang oder das Ende der Nachricht angehängt wird.")
		.icon("labymod:settings/settings/marker")
		.defaultValue(TagPosition.AFTER);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Echtgeld- Erkennung") // Spacing to allow word wrap
		.description("Fügt einen Tag zu eingehenden Bezahlungen hinzu.")
		.icon("coin_pile")
		.subSettings(tag, position);

	@EventListener(priority = Priority.LOWEST)
	public void onMessageReceive(MessageModifyEvent event) {
		if (!Constants.PAYMENT_RECEIVE_PATTERN.matcher(event.original.getFormattedText()).matches())
			return;

		String text = "§r" + tag.get().replace('&', '§') + "§r";

		if (position.get() == TagPosition.BEFORE)
			event.setMessage(new ChatComponentText(text).appendSibling(event.message));
		else
			event.setMessage(event.message.appendText(text));
	}

	private enum TagPosition implements Named {

		BEFORE("Davor"), AFTER("Danach");

		private final String name;
		TagPosition(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
