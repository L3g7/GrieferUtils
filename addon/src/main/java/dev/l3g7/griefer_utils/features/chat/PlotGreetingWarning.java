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

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import io.netty.channel.ChannelHandler;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class PlotGreetingWarning extends Feature {

	private static final String GREETING_PATTERN = "^§r§8\\[§r§6GrieferGames§r§8] §r§8\\[§r§6-?\\d+;-?\\d+§r§8] ";
	private long lastReadTime = -1;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Plot-Greeting-Warnung")
		.description("Fügt einen Tag zu Nachrichten hinzu, die von einem Plot-Greeting kommen.")
		.icon("coin_pile");

	@EventListener
	public void onMessageModify(MessageModifyEvent event) {
		if (event.original.getFormattedText().matches(GREETING_PATTERN + ".*")) {
			lastReadTime = getLastReadTime();

			// Add warning if text follows
			if (!event.original.getFormattedText().matches(GREETING_PATTERN + "§r$"))
				addWarning(event.message);

			return;
		}

		if (lastReadTime == -1)
			return;

		if (lastReadTime != getLastReadTime()) {
			lastReadTime = -1;
			return;
		}

		if (!event.original.getFormattedText().equals("§r§8[§r§6GrieferGames§r§8] §r§aDu wurdest zum Grundstück teleportiert.§r"))
			addWarning(event.message);
	}

	private void addWarning(IChatComponent message) {
		IChatComponent icc = new ChatComponentText("§e [⚠]");
		icc.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§eDiese Nachricht stammt von einem Plot-Gretting.")));
		message.appendSibling(icc);
	}

	private long getLastReadTime() {
		ChannelHandler timeoutHandler = mc().getNetHandler().getNetworkManager().channel().pipeline().get("timeout");
		return Reflection.get(timeoutHandler, "lastReadTime");
	}

}
