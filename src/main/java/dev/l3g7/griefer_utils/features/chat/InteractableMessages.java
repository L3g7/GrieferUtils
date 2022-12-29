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
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.ModColor;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.util.misc.Constants.FORMATTED_PLAYER_PATTERN;

@Singleton
public class InteractableMessages extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Interagierbare Nachrichten")
		.description("Macht TPAs, den CityBuild bei Globalchat-Nachrichten und den Status interagierbar.")
		.icon("cursor");

	private static final String COMMAND_PREFIX = "/griefer_utils_interactable_messages";
	private static final String TP_ACCEPT = "Um die Anfrage anzunehmen, schreibe /tpaccept.";
	private static final String TP_DENY = "Um sie abzulehnen, schreibe /tpdeny.";
	public static final Pattern STATUS_PATTERN = Pattern.compile(String.format("^%s§f (?<message>[^\u00bb]*)§*r*$", FORMATTED_PLAYER_PATTERN));
	private static final Map<String, String> GLOBALCHAT_CB_TO_SWITCH = new HashMap<String, String>(){{
		put("CBE", "cbevil");
		put("WASSER", "farm1");
		put("LAVA", "nether1");
		put("EVENT", "eventserver");
	}};

	@EventListener(priority = EventPriority.LOW)
	public void modifyMessage(MessageModifyEvent event) {
		modifyGlobalChats(event);
		modifyStatuses(event);
		modifyTps(event);
	}

	@EventListener(triggerWhenDisabled = true)
	public void onSend(MessageEvent.MessageSendEvent event) {

	}

	private static void modifyGlobalChats(MessageModifyEvent event) {
		String unformattedText = event.message.getUnformattedText();
		if (!unformattedText.startsWith("@["))
			return;

		String cb = unformattedText.substring(2, unformattedText.indexOf(']'));

		IChatComponent message = event.message;

		for (IChatComponent sibling : message.getSiblings()) {
			if (!sibling.getUnformattedTextForChat().equals(cb))
				continue;

			sibling.getChatStyle()
				.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/switch " + GLOBALCHAT_CB_TO_SWITCH.getOrDefault(cb, cb)))
				.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§6Klicke, um auf den CB zu wechseln")));
			break;
		}

		event.message = message;
	}

	private static void modifyStatuses(MessageModifyEvent event) {
		String formattedText = event.message.getFormattedText();
		Matcher matcher = STATUS_PATTERN.matcher(formattedText);
		if (!matcher.matches())
			return;

		String name = matcher.group("name");
		IChatComponent message = event.message;

		for (IChatComponent part : message.getSiblings())
			part.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/msg %s ", ModColor.removeColor(name))));

		event.message = message;
	}

	private static void modifyTps(MessageModifyEvent event) {
		String msg = event.message.getUnformattedText();

		if (!msg.equals(TP_ACCEPT) && !msg.equals(TP_DENY))
			return;

		String command = msg.equals(TP_ACCEPT) ? "/tpaccept" : "/tpdeny";

		IChatComponent component = event.message;

		for (IChatComponent part : component.getSiblings())
			if (part.getUnformattedText().equals(command))
				part.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));

		event.message = component;
	}

}
