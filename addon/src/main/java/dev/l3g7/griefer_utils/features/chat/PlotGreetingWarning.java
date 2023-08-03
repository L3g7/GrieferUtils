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
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import io.netty.channel.ChannelHandler;
import net.minecraft.event.HoverEvent;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class PlotGreetingWarning extends Feature {

	private static final String GREETING_PATTERN = "^§r§8\\[§r§6GrieferGames§r§8] §r§8\\[§r§6-?\\d+;-?\\d+§r§8] ";
	private long lastGreetingReadTime = 0;

	private final NumberSetting maxTimeDifference = new NumberSetting()
		.name("Max. Zeitabstand (µs)")
		.description("Wie viel Zeit (in Mikrosekunden) maximal zwischen zwei Plot-Greeting-Nachrichten vergehen darf.")
		.icon("hourglass")
		.defaultValue(256);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Warnung bei Plot-Greeting")
		.description("Fügt einen Tag zu Nachrichten hinzu, die von einem Plot-Greeting kommen.")
		.icon("labymod:buttons/exclamation_mark")
		.subSettings(maxTimeDifference);

	@EventListener
	public void onMessageModify(PacketEvent.PacketReceiveEvent event) {
		if (!(event.packet instanceof S02PacketChat))
			return;

		long lastReadTime = getLastReadTime();
		IChatComponent message = ((S02PacketChat) event.packet).getChatComponent();

		// Check if message was begin of greeting
		if (message.getFormattedText().matches(GREETING_PATTERN + ".*")) {
			lastGreetingReadTime = lastReadTime;

			// Mark plot greeting if it has additional text
			if (!message.getFormattedText().matches(GREETING_PATTERN + "§r$"))
				addWarning(message, true);

			return;
		}

		// Guess whether message was part of greeting

		// Check if message was within specified time
		if (lastGreetingReadTime != 0 && lastReadTime - lastGreetingReadTime <= maxTimeDifference.get() * 1000) {

			// Check if message is nested
			if (message.getSiblings().stream().anyMatch(c -> !c.getSiblings().isEmpty()))
				return;

			// Check if message contains events
			if (message.getSiblings().stream().anyMatch(v -> v.getChatStyle().getChatHoverEvent() != null || v.getChatStyle().getChatClickEvent() != null))
				return;

			// Check if message contains illegal characters
			String textMessage = message.getUnformattedText();
			if (!textMessage.equals(ChatAllowedCharacters.filterAllowedCharacters(textMessage)) || textMessage.contains("»") || textMessage.contains("┃"))
				return;

			// Message seems to be part of a greeting
			addWarning(message, false);
		}
	}

	private void addWarning(IChatComponent message, boolean sure) {
		IChatComponent icc = new ChatComponentText(" [⚠]");
		icc.getChatStyle().setColor(EnumChatFormatting.YELLOW);
		icc.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§eDiese Nachricht stammt " + (sure ? "" : "wahrscheinlich ") + "von einem Plot-Greeting.")));
		message.appendSibling(icc);
	}

	private long getLastReadTime() {
		if (mc().getNetHandler() == null)
			return 0;

		ChannelHandler timeoutHandler = mc().getNetHandler().getNetworkManager().channel().pipeline().get("timeout");
		return Reflection.get(timeoutHandler, "lastReadTime");
	}

}
