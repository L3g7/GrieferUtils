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

package dev.l3g7.griefer_utils.features.chat;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.event_bus.Priority;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.Citybuild;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.misc.Constants.*;
import static net.minecraft.event.ClickEvent.Action.RUN_COMMAND;

@Singleton
public class InteractableMessages extends Feature {

	private static final String TP_ACCEPT = "Um die Anfrage anzunehmen, schreibe /tpaccept.";
	private static final String TP_DENY = "Um sie abzulehnen, schreibe /tpdeny.";
	private static final Pattern P_H_PATTERN = Pattern.compile("^.*(?<command>/p h [^ ]+).*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern CLAN_INFO_PATTERN = Pattern.compile("^» (?<name>[^ ]+) \\((?:offline|online)\\)$");
	private static final Pattern PLOT_INFO_PLAYER_PATTERN = Pattern.compile("^§r§7(?:Besitzer|Helfer|Vertraut|Verboten): .+$");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Interagierbare Nachrichten")
		.description("Macht Folgendes interagierbar:"
			+ "\n- TPAs (§a/tpaccept§r und §c/tpdeny§r)"
			+ "\n- Den Citybuild bei Globalchat-Nachrichten (Switcht zum CB)"
			+ "\n- Den Status, Msgs, und Plotchat-Nachrichten (Schlägt /msg vor)"
			+ "\n- \"/p h\" in Nachrichten (Teleportiert zum Plot)"
			+ "\n- Spielernamen bei /clan info (Öffnet das Profil)"
			+ "\n- Spielernamen bei /p i (Öffnet das Profil)")
		.icon("left_click");

	@EventListener(priority = Priority.LOW)
	public void modifyMessage(MessageModifyEvent event) {
		modifyGlobalChats(event);
		modifyTps(event);
		modifyPHs(event);
		modifyClanInfo(event);
		modifyPlotInfo(event);
		addMsgSuggestions(event);
	}

	private void modifyGlobalChats(MessageModifyEvent event) {
		String unformattedText = event.original.getUnformattedText();
		if (!unformattedText.startsWith("@["))
			return;

		String cb = unformattedText.substring(2, unformattedText.indexOf(']'));

		IChatComponent message = event.message;

		for (IChatComponent sibling : message.getSiblings()) {
			if (!sibling.getUnformattedTextForChat().equals(cb))
				continue;

			sibling.getChatStyle()
				.setChatClickEvent(new ClickEvent(RUN_COMMAND, "/switch " + Citybuild.getCitybuild(cb).getInternalName()))
				.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§6Klicke, um auf den CB zu wechseln")));
			break;
		}

		event.message = message;
	}

	private void modifyTps(MessageModifyEvent event) {
		String msg = event.original.getUnformattedText();

		if (!msg.equals(TP_ACCEPT) && !msg.equals(TP_DENY))
			return;

		String command = msg.equals(TP_ACCEPT) ? "/tpaccept" : "/tpdeny";

		IChatComponent component = event.message;

		for (IChatComponent part : component.getSiblings())
			if (part.getUnformattedText().equals(command))
				part.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, command));

		event.message = component;
	}

	private void modifyPHs(MessageModifyEvent event) {
		Matcher matcher = P_H_PATTERN.matcher(event.message.getUnformattedText());
		if (!matcher.matches())
			return;

		String command = matcher.group("command");
		String unformatted = event.message.getUnformattedText();

		int startIndex = unformatted.indexOf(command);
		int endIndex = startIndex + command.length();
		int i = 0;

		List<IChatComponent> components = new ArrayList<>();
		components.add(event.message);

		ListIterator<IChatComponent> it = components.listIterator();
		while (it.hasNext()) {
			IChatComponent t = it.next();
			t.getSiblings().forEach(it::add);
			t.getSiblings().forEach(s -> it.previous());
		}

		for (IChatComponent c : components) {
			if (!(c instanceof ChatComponentText))
				return;

			ChatStyle style = c.getChatStyle().createDeepCopy();

			String text = c.getUnformattedTextForChat();
			int len = text.length();
			if (i >= startIndex && i + len <= endIndex) { // The entire component is part of the command
				c.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, command));
			} else if (i <= startIndex && len + i > startIndex) { // Command is at the end
				Reflection.set(c, text.substring(0, startIndex - i), "text");
				IChatComponent commandComponent = new ChatComponentText(text.substring(startIndex - i));
				setStyle(commandComponent, style, c.getFormattedText());
				commandComponent.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, command));
				c.getSiblings().add(0, commandComponent);
				c = commandComponent;
				text = commandComponent.getUnformattedTextForChat();
				len = text.length();
				i = startIndex;
			}
			if (i + len >= endIndex) {
				int index = i >= startIndex ? endIndex : startIndex;
				if (i + len > index) { // Command is at the start
					Reflection.set(c, text.substring(0, index - i), "text");
					IChatComponent postComponent = new ChatComponentText(text.substring(index - i));
					setStyle(postComponent, style, c.getFormattedText());
					c.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, command));
					c.getSiblings().add(0, postComponent);
				}
				break;
			}
			i += len;
		}
	}

	private static void setStyle(IChatComponent target, ChatStyle style, String fallbackText) {
		if (!style.equals(new ChatStyle())) {
			target.setChatStyle(style);
			return;
		}

		while (fallbackText.endsWith("§r"))
			fallbackText = fallbackText.substring(0, fallbackText.length() - 2);

		StringBuilder prefix = new StringBuilder();
		Matcher matcher = Pattern.compile("§(.)").matcher(fallbackText);
		while (matcher.find()) {
			String formatting = matcher.group(1);
			if (formatting.matches("[0-9a-fr]"))
				prefix = new StringBuilder();

			if (!formatting.equals("r"))
				prefix.append('§').append(formatting);
		}

		Reflection.set(target, prefix + target.getUnformattedText(), "text");
	}

	private void modifyClanInfo(MessageModifyEvent event) {
		String unformattedText = event.original.getUnformattedText();
		Matcher matcher = CLAN_INFO_PATTERN.matcher(unformattedText);
		if (!matcher.matches())
			return;

		String name = matcher.group("name");
		IChatComponent message = event.message;

		for (IChatComponent part : message.getSiblings())
			part.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, String.format("/profil %s ", name)));

		event.message = message;
	}

	private void modifyPlotInfo(MessageModifyEvent event) {
		if (!PLOT_INFO_PLAYER_PATTERN.matcher(event.original.getFormattedText()).matches())
			return;

		List<IChatComponent> siblings = event.message.getSiblings();

		boolean modify = false;
		for (IChatComponent sibling : siblings) {
			if (sibling.getUnformattedText().endsWith(": "))
				modify = true;
			else if (modify) {
				if (sibling.getChatStyle().getColor() != EnumChatFormatting.YELLOW) // red = invalid, gray = empty
					continue;

				if (!sibling.getUnformattedText().trim().isEmpty())
					sibling.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, "/profil " + sibling.getUnformattedText()));
			}
		}
	}

	public void addMsgSuggestions(MessageModifyEvent event) {
		String text = event.original.getFormattedText();

		for (Pattern p : new Pattern[] {PLOTCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, STATUS_PATTERN}) {
			Matcher matcher = p.matcher(text);
			if (!matcher.find())
				continue;

			String name = matcher.group("name").replaceAll("§.", "");
			event.message.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/msg %s ", name)));
			return;
		}
	}

}
