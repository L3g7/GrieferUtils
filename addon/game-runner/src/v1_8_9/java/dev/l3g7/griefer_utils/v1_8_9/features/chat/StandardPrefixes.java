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

import dev.l3g7.griefer_utils.api.BugReporter;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.Priority;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.TabListEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.misc.NameCache;
import dev.l3g7.griefer_utils.v1_8_9.util.IChatComponentUtil;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Items;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.*;

import static dev.l3g7.griefer_utils.v1_8_9.util.IChatComponentUtil.getComponents;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.name;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

@Singleton
public class StandardPrefixes extends Feature {

	private static final Map<String, String> DEFAULT_PREFIXES = new HashMap<String, String>() {{
		put("Helfer", "2");
		put("Supreme", "dl");
		put("Griefer", "4l");
		put("Titan", "9");
		put("Legende", "c");
		put("Ultra", "b");
		put("Premium", "6");
		put("Spieler", "7");
	}};

	private final SwitchSetting tab = SwitchSetting.create()
		.name("In Tabliste")
		.description("Ob die Prefixe in der Tabliste geändert werden sollen.")
		.icon("tab_list")
		.defaultValue(true)
		.callback(TabListEvent::updatePlayerInfoList);

	private final SwitchSetting chat = SwitchSetting.create()
		.name("Im Chat")
		.description("Ob die Prefixe im Chat geändert werden sollen.")
		.icon("speech_bubble")
		.defaultValue(true)
		.callback(enabled -> {
			if (!enabled && !tab.get())
				tab.set(true);
		});

	private final SwitchSetting self = SwitchSetting.create()
		.name("Eigenen Prefix ändern")
		.description("Ob der eigene Prefix auch geändert werden sollen.")
		.icon("steve")
		.defaultValue(true)
		.callback(TabListEvent::updatePlayerInfoList);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Standard-Prefixe")
		.description("Setzt die Prefixe jeder Person auf den standard Prefix des jeweiligen Ranges.")
		.icon(Items.name_tag)
		.callback(TabListEvent::updatePlayerInfoList)
		.subSettings(chat, tab, self);

	@Override
	public void init() {
		super.init();
		getCategory().getSetting().callback(TabListEvent::updatePlayerInfoList);
		tab.callback(enabled -> {
			if (!enabled && !chat.get())
				chat.set(true);
		});
	}

	@EventListener(priority = Priority.HIGHEST)
	public void onTabListNameUpdate(TabListEvent.TabListNameUpdateEvent event) {
		if (!tab.get() || !event.component.getUnformattedText().contains("\u2503"))
			return;

		String unformatted = event.component.getUnformattedText().replaceAll("§.", "");

		String[] parts = unformatted.split(" \u2503 ");

		if (parts.length != 2) {
			BugReporter.reportError(new Throwable(event.profile + " + / " + IChatComponent.Serializer.componentToJson(event.component)));
			return;
		}

		if (!self.get() && player() != null && name().equals(NameCache.ensureRealName(parts[1])))
			return;

		String prefix = DEFAULT_PREFIXES.get(parts[0]);
		if (prefix == null)
			return;

		IChatComponentUtil.setNameWithPrefix(event.component, parts[1], parts[1], prefix, true);
		setRankWithPrefix(event.component, parts[0], prefix, true);
	}

	@EventListener(priority = Priority.HIGHEST)
	public void onMessageModifyChat(MessageEvent.MessageModifyEvent event) {
		if (!chat.get())
			return;

		String text = event.message.getUnformattedText();

		if (!text.contains("\u2503"))
			return;

		String name = text.substring(text.indexOf('\u2503') + 2);
		int bracketIndex = name.indexOf(']') == -1 ? Integer.MAX_VALUE : name.indexOf(']');
		int spaceIndex = name.indexOf(' ');

		if (spaceIndex == -1 && bracketIndex == Integer.MAX_VALUE)
			return;

		name = name.substring(0, Math.min(spaceIndex, bracketIndex));

		if (!self.get() && name().equals(NameCache.ensureRealName(name)))
			return;

		String rank = text.substring(0, text.indexOf('\u2503') - 1);
		int startBracketIndex = rank.lastIndexOf('[');
		int startSpaceIndex = rank.lastIndexOf(' ');

		if (startBracketIndex != -1 || startSpaceIndex != -1)
			rank = rank.substring(Math.max(startSpaceIndex, startBracketIndex) + 1);

		String prefix = DEFAULT_PREFIXES.get(rank);
		if (prefix == null)
			return;

		IChatComponentUtil.setNameWithPrefix(event.message, name, name, prefix, false);
		setRankWithPrefix(event.message, rank, prefix, false);
	}

	private void setRankWithPrefix(IChatComponent iChatComponent, String rank, String prefix, boolean isTabList) {
		List<IChatComponent> everything = iChatComponent.getSiblings();
		if (everything.isEmpty()) {
			BugReporter.reportError(new Throwable("No siblings in " + IChatComponent.Serializer.componentToJson(iChatComponent)));
			return;
		}

		IChatComponent parent = everything.get(everything.size() - 1);

		if (parent.getSiblings().isEmpty())
			parent = iChatComponent;

		List<IChatComponent> lastSiblings = parent.getSiblings();

		int startIndex = 0;
		boolean reachedSeparator = false;

		for (ListIterator<IChatComponent> iterator = lastSiblings.listIterator(); reachedSeparator ? iterator.hasPrevious() : iterator.hasNext(); ) {
			String text = (reachedSeparator ? iterator.previous() : iterator.next()).getUnformattedTextForChat();

			if (!reachedSeparator) {
				if (text.equals("\u2503 ")) {
					reachedSeparator = true;

					// The json is pretty wierd
					iterator.previous();
					iterator.previous();
					startIndex = iterator.previousIndex();
					iterator.remove();
				}
				continue;
			}

			if (text.contains(" ") || text.contains("[") || text.contains("]")) {
				startIndex++;
				break;
			} else {
				startIndex = iterator.previousIndex();
				iterator.remove();
			}
		}

		Collection<IChatComponent> nameComponents = new ArrayList<>(getComponents(rank, prefix, isTabList));

		// Add the HoverEvent and make it italic
		ClickEvent clickEvent = parent.getChatStyle().getChatClickEvent();

		for (IChatComponent component : nameComponents) {

			if (clickEvent != null)
				component.getChatStyle().setChatClickEvent(clickEvent);
		}

		nameComponents.add(new ChatComponentText(" "));

		parent.getChatStyle().setChatClickEvent(clickEvent);
		lastSiblings.addAll(Math.max(startIndex, 0), nameComponents);
	}

}
