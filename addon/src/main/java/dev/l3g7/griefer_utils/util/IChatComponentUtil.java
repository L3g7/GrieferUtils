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

package dev.l3g7.griefer_utils.util;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageSendEvent;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.displayAchievement;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.suggest;

/**
 * A utility class for methods related to {@link IChatComponent}s.
 */
public class IChatComponentUtil {

	private static final String COMMAND = "/grieferutils_namehistory_prompt_fix ";

	@EventListener
	private static void onMessageSend(MessageSendEvent event) {
		if (event.message.startsWith(COMMAND)) {
			suggest(event.message.substring(COMMAND.length()));
			event.cancel();
		}
	}

	public static void setNameWithPrefix(IChatComponent iChatComponent, String name, String realName, String prefix, boolean isTabList) {
		List<IChatComponent> everything = iChatComponent.getSiblings();
		IChatComponent parent = everything.size() > 0 ? everything.get(everything.size() - 1) : iChatComponent;

		if (parent.getSiblings().isEmpty())
			parent = iChatComponent;

		List<IChatComponent> lastSiblings = parent.getSiblings();

		int playerIndex = -1;

		for (ListIterator<IChatComponent> iterator = lastSiblings.listIterator(); iterator.hasNext(); ) {
			String text = iterator.next().getUnformattedTextForChat();

			if (playerIndex == -1) {
				if (text.matches(" ?\u2503 "))
					playerIndex = iterator.nextIndex();
				continue;
			}

			if (text.contains(" ") || text.contains("]"))
				break;
			else
				iterator.remove();
		}

		if (playerIndex == -1) {
			new Throwable().printStackTrace();
			System.err.println("IChatComponentUtil error:");
			System.err.println(IChatComponent.Serializer.componentToJson(iChatComponent));
			System.err.println("name = " + name + ", realName = " + realName + ", prefix = " + prefix + ", isTabList = " + isTabList);
			displayAchievement("§c§lFehler \u26A0", "§cBitte melde dich beim Team.");
			return;
		}

		Collection<IChatComponent> nameComponents = getComponents(realName, prefix, isTabList);

		ChatComponentText nickName = new ChatComponentText("");
		getComponents(name, prefix, false).forEach(nickName::appendSibling);

		// Add the HoverEvent and make it italic
		ClickEvent clickEvent = parent.getChatStyle().getChatClickEvent();
		boolean withHover = !realName.equals(name);

		for (IChatComponent component : nameComponents) {
			if (withHover)
				component.getChatStyle()
					.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, nickName))
					.setItalic(true);

			if (clickEvent != null)
				component.getChatStyle().setChatClickEvent(withHover ? new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMMAND + clickEvent.getValue()) : clickEvent);
		}

		parent.getChatStyle().setChatClickEvent(clickEvent);
		lastSiblings.addAll(playerIndex, nameComponents);
	}

	public static Collection<IChatComponent> getComponents(String text, String formatted, boolean isTabList) {
		if (text == null)
			return ImmutableList.of(new ChatComponentText("404").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)));

		if (formatted.length() <= 2) {
			ChatStyle style = new ChatStyle()
				.setColor(EnumChatFormatting.func_175744_a(Integer.parseInt(String.valueOf(formatted.charAt(0)), 16)));
			if (formatted.contains("l") && !isTabList)
				style.setBold(true);
			if (formatted.contains("k"))
				style.setObfuscated(true);

			return ImmutableList.of(new ChatComponentText(text).setChatStyle(style));
		}

		List<IChatComponent> components = new ArrayList<>();
		char[] chars = formatted.toCharArray();

		for (int i = 0; i < text.toCharArray().length; i++) {

			ChatStyle style = new ChatStyle()
				.setColor(EnumChatFormatting.func_175744_a(Integer.parseInt(String.valueOf(chars[i % chars.length]), 16)));

			if (!isTabList)
				style.setBold(true);

			String content = String.valueOf(text.charAt(i));
			if (chars[i % chars.length] == chars[(i + 1) % chars.length] && text.length() != i + 1)
				content += text.charAt(++i);

			components.add(new ChatComponentText(content).setChatStyle(style));
		}

		return components;
	}

}
