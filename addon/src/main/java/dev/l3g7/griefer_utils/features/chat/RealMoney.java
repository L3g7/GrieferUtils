/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.chat;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.event_bus.Priority;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

@Singleton
public class RealMoney extends Feature {

	private final StringSetting tag = new StringSetting()
		.name("Tag")
		.icon(Material.NAME_TAG)
		.defaultValue("&a [✔]");

	private final DropDownSetting<TagPosition> position = new DropDownSetting<>(TagPosition.class)
		.name("Position")
		.icon("labymod:settings/settings/marker")
		.defaultValue(TagPosition.AFTER);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Echtgeld- Erkennung") // Spacing to allow word wrap
		.description("Fügt einen Tag zu eingehenden Bezahlungen hinzu.")
		.icon("coin_pile")
		.subSettings(tag, position);

	@EventListener(priority = Priority.LOWEST)
	public void onMessageReceive(MessageReceiveEvent event) {
		if (Constants.PAYMENT_RECEIVE_PATTERN.matcher(event.message.getFormattedText()).matches()) {
			String text = "§r" + tag.get().replace('&', '§') + "§r";

			IChatComponent message;
			if (position.get() == TagPosition.BEFORE)
				message = new ChatComponentText(text).appendSibling(event.message);
			else
				message = event.message.appendText(text);

			// Feed new message into packet pipeline to allow reprocessing
			Minecraft.getMinecraft().getNetHandler().handleChat(new S02PacketChat(message));

			event.cancel();
		}
	}

	private enum TagPosition {

		BEFORE("Davor"), AFTER("Danach");

		private final String name;
		TagPosition(String name) {
			this.name = name;
		}
	}

}
