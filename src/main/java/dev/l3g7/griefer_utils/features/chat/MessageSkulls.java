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


import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.misc.NameCache;
import net.labymod.ingamechat.renderer.ChatLine;
import net.labymod.main.LabyMod;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.displayAchievement;
import static dev.l3g7.griefer_utils.util.misc.Constants.*;

@Singleton
public class MessageSkulls extends Feature {

	private static final String SPACE = "§m§e§s§s§a§g§e§s§k§u§l§l§s§r   ";
	private static final ArrayList<Pattern> PATTERNS = new ArrayList<Pattern>(MESSAGE_PATTERNS) {{
		remove(GLOBAL_CHAT_PATTERN);
		add(STATUS_PATTERN);
	}};

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Kopf vor Nachrichten")
		.description("Zeigt den Kopf des Autors vor Nachrichten an.")
		.icon("steve");

	@EventListener
	public void onMsgReceive(MessageEvent.MessageModifyEvent event) {
		for (Pattern pattern : PATTERNS) {
			Matcher matcher = pattern.matcher(event.message.getFormattedText());

			if (matcher.matches()) {
				event.message = new ChatComponentText(SPACE).appendSibling(event.message);
				return;
			}
		}
	}

	@SuppressWarnings("unused")
	public static void renderSkull(ChatLine line, int y, float alpha) {

		IChatComponent component = (IChatComponent) line.getComponent();
		String formattedText = component.getFormattedText();
		int spaceIndex = formattedText.indexOf(SPACE);

		if (spaceIndex == -1)
			return;

		String msg = ModColor.removeColor(formattedText.substring(spaceIndex + SPACE.length()));

		int startIndex = msg.indexOf('\u2503') + 2;
		int endIndex;
		int arrowIndex = msg.indexOf('\u00bb');

		if (arrowIndex != -1)
			endIndex = arrowIndex - 1;
		else if (msg.startsWith("[Plot-Chat]"))
			endIndex = msg.indexOf(':') - 1;
		else if (msg.startsWith("[") && msg.contains(" -> mir]"))
			endIndex = msg.indexOf('-') - 1;
		else if (msg.startsWith("[mir -> "))
			endIndex = msg.indexOf(']');
		else
			endIndex = msg.indexOf(' ', startIndex);

		if ((endIndex - startIndex) < 0) {
			System.err.println(startIndex + " " + endIndex + " | " + IChatComponent.Serializer.componentToJson(component));
			displayAchievement("§c§lFehler \u26A0", "§cBitte melde dich beim Team.");
			return;
		}

		String name = msg.substring(startIndex, endIndex);
		NetworkPlayerInfo playerInfo = MinecraftUtil.mc().getNetHandler().getPlayerInfo(NameCache.ensureRealName(name));
		if (playerInfo == null)
			return;


		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();
		drawUtils.bindTexture(playerInfo.getLocationSkin());
		int x = drawUtils.getStringWidth(formattedText.substring(0, spaceIndex)) + (formattedText.startsWith("§r" + SPACE) ? 2 : 1);
		drawUtils.drawTexture(x, y - 8, 32, 32, 32, 32, 8, 8, alpha); // First layer
		drawUtils.drawTexture(x, y - 8, 160, 32, 32, 32, 8, 8, alpha); // Second layer
	}

}