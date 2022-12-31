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

package dev.l3g7.griefer_utils.features.chat.auto_unnick;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import dev.l3g7.griefer_utils.util.misc.Constants;
import net.labymod.utils.Material;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.event.events.network.TabListEvent.updatePlayerInfoList;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.suggest;

@Singleton
public class AutoUnnick extends Feature {

	private static final String COMMAND = "/grieferutils_autounnick_namehistory_prompt_fix ";

	private final BooleanSetting tab = new BooleanSetting()
		.name("In Tabliste")
		.description("Ob Spieler in der Tabliste entnickt werden sollen.")
		.icon("tab_list")
		.callback(c -> updatePlayerInfoList());

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Automatisch entnicken")
		.description("Zeigt statt Nicks immer den echten Namen in Chatnachrichten.")
		.icon(Material.NAME_TAG)
		.callback(c -> updatePlayerInfoList())
		.subSettings(tab);

	@Override
	public void init() {
		super.init();
		getCategory().getSetting().addCallback(v -> TabListEvent.updatePlayerInfoList());
	}


	@EventListener
	public void onMessageSend(MessageSendEvent event) {
		if (event.message.startsWith(COMMAND)) {
			suggest(event.message.substring(COMMAND.length()));
			event.setCanceled(true);
		}
	}

	@EventListener(priority = EventPriority.HIGH)
	public void onTabListNameUpdate(TabListEvent.TabListNameUpdateEvent event) {
		if (!tab.get())
			return;

		String text = event.component.getUnformattedText();

		if (!text.contains("~"))
			return;

		String nickName = text.substring(text.indexOf('~'));
		String[] parts = event.component.getFormattedText().split(" §r§8\u2503 §r");

		String realName = event.profile.getName();
		if (player().getUniqueID().equals(event.profile.getId()))
			realName = player().getName();
		setNameWithPrefix(event.component, parts[0], parts[1], nickName, realName, true);
	}

	@EventListener(priority = EventPriority.HIGH)
	public void onMessageModifyChat(MessageEvent.MessageModifyEvent event) {
		String text = event.message.getUnformattedText();

		if (!text.contains("\u2503") || !text.contains("~"))
			return;

		String name = text.substring(text.indexOf('\u2503') + 2);
		int bracketIndex = name.indexOf(']') == -1 ? Integer.MAX_VALUE : name.indexOf(']');
		int spaceIndex = name.indexOf(' ');

		if (spaceIndex == -1 && bracketIndex == Integer.MAX_VALUE)
			return;

		name = name.substring(0, Math.min(spaceIndex, bracketIndex));

		if (!name.contains("~"))
			return;

		for (Pattern pattern : Constants.MESSAGE_PATTERNS) {
			Matcher matcher = pattern.matcher(event.message.getFormattedText());

			if (matcher.matches()) {
				setNameWithPrefix(event.message, matcher.group("rank"), matcher.group("name"), name, PlayerUtil.unnick(name), false);
				return;
			}
		}
	}

	private void setNameWithPrefix(IChatComponent iChatComponent, String rank, String formattedName, String unformattedName, String unnickedName, boolean isTabList) {
		List<IChatComponent> everything = iChatComponent.getSiblings();
		IChatComponent parent = everything.get(everything.size() - 1);

		if (parent.getSiblings().isEmpty())
			parent = iChatComponent;

		List<IChatComponent> lastSiblings = parent.getSiblings();

		int playerIndex = -1;

		for (ListIterator<IChatComponent> iterator = lastSiblings.listIterator(); iterator.hasNext(); ) {
			String text = iterator.next().getUnformattedTextForChat();

			if (playerIndex == -1) {
				if (text.equals("\u2503 "))
					playerIndex = iterator.nextIndex();
				continue;
			}

			if (text.contains(" ") || text.contains("]"))
				break;
			else
				iterator.remove();
		}

		String prefix = new PrefixFinder(rank, formattedName).getPrefix();

		Collection<IChatComponent> name = getNameComponents(unnickedName, prefix, isTabList);
		ClickEvent clickEvent = parent.getChatStyle().getChatClickEvent();

		ChatComponentText nickName = new ChatComponentText("");
		getNameComponents(unformattedName, prefix, false).forEach(nickName::appendSibling);

		// Add the HoverEvent and make it italic
		for (IChatComponent component : name) {
			component.getChatStyle()
				.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, nickName))
				.setItalic(true);

			if (clickEvent != null)
				component.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMMAND + clickEvent.getValue()));
		}

		parent.getChatStyle().setChatClickEvent(clickEvent);

		lastSiblings.addAll(playerIndex, name);
	}

	private Collection<IChatComponent> getNameComponents(String name, String prefix, boolean isTabList) {

		if (name == null)
			return ImmutableList.of(new ChatComponentText("404").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)));

		if (prefix.length() <= 2) {
			ChatStyle style = new ChatStyle()
				.setColor(EnumChatFormatting.func_175744_a(Integer.parseInt(String.valueOf(prefix.charAt(0)), 16)));
			if (prefix.contains("l") && !isTabList)
				style.setBold(true);
			if (prefix.contains("k"))
				style.setObfuscated(true);

			return ImmutableList.of(new ChatComponentText(name).setChatStyle(style));
		}

		List<IChatComponent> components = new ArrayList<>();
		char[] chars = prefix.toCharArray();

		for (int i = 0; i < name.toCharArray().length; i++) {

			ChatStyle style = new ChatStyle()
				.setColor(EnumChatFormatting.func_175744_a(Integer.parseInt(String.valueOf(chars[i % chars.length]), 16)));

			if (!isTabList)
				style.setBold(true);

			String text = String.valueOf(name.charAt(i));
			if (chars[i % chars.length] == chars[(i + 1) % chars.length] && name.length() != i + 1)
				text += name.charAt(++i);

			components.add(new ChatComponentText(text).setChatStyle(style));
		}

		return components;
	}

}
