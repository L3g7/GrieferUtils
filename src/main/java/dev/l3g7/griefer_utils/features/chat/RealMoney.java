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

package dev.l3g7.griefer_utils.features.chat;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.RadioSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.util.misc.Constants;
import net.labymod.utils.Material;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class RealMoney extends Feature {

	private final StringSetting tag = new StringSetting()
		.name("Tag")
		.icon(Material.NAME_TAG)
		.defaultValue("&a [✔]");

	private final RadioSetting<TagPosition> position = new RadioSetting<>(TagPosition.class)
		.name("Position")
		.icon("labymod:settings/settings/marker")
		.defaultValue(TagPosition.AFTER);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("RealMoney")
		.icon("coin_pile")
		.subSettings(tag, position);

	@EventListener(priority = EventPriority.LOWEST)
	public void onMessageReceive(ClientChatReceivedEvent event) {
		if (Constants.PAYMENT_RECEIVE_PATTERN.matcher(event.message.getFormattedText()).matches()) {
			String text = "§r" + tag.get().replace('&', '§') + "§r";

			if (position.get() == TagPosition.BEFORE)
				player().addChatMessage(new ChatComponentText(text).appendSibling(event.message));
			else
				player().addChatMessage(event.message.appendText(text));

			event.setCanceled(true);
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
