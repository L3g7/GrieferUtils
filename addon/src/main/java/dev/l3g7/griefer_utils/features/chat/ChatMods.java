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

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.utils.Material;
import net.minecraft.init.Blocks;

import java.util.List;

@Singleton
public class ChatMods extends Feature {

	private static final List<String> MYSTERY_MOD_DOWNLOAD_NOTIFICATION = ImmutableList.of(
		"§r§8[§r§6GrieferGames§r§8] §r§cOhje. Du benutzt noch kein MysteryMod!§r",
		"§r§8[§r§6GrieferGames§r§8] §r§fWir sind optimiert für MysteryMod und die neusten Funktionen hast Du nur damit.§r",
		"§r§8[§r§6GrieferGames§r§8] §r§fDownload: §r§ahttps://mysterymod.net/download/§r",
		"§r§8[§r§6GrieferGames§r§8] §r§fWir sind optimiert für MysteryMod. Lade Dir gerne die Mod runter!§r"
	);

	private final BooleanSetting antiClearChat = new BooleanSetting()
		.name("Clearchat unterbinden")
		.icon(Material.BARRIER)
		.defaultValue(true);

	private final BooleanSetting removeSupremeSpaces = new BooleanSetting()
		.name("Supreme-Leerzeichen entfernen")
		.icon(Material.BARRIER)
		.defaultValue(true);

	private final DropDownSetting<NewsMode> news = new DropDownSetting<>(NewsMode.class)
		.name("News")
		.icon("exclamation_mark")
		.icon("labymod:buttons/exclamation_mark")
		.defaultValue(NewsMode.NORMAL);

	private final BooleanSetting removeStreamerNotifications = new BooleanSetting()
		.name("Streamer-Benachrichtigungen entfernen")
		.icon("twitch");

	private final BooleanSetting removeMysteryMod = new BooleanSetting()
		.name("Download-Benachrichtigungen entfernen")
		.icon("mysterymod")
		.defaultValue(true);

	private final BooleanSetting removeLuckyBlock = new BooleanSetting()
		.name("LuckyBlock-Benachrichtigungen entfernen")
		.icon(ItemUtil.createItem(Blocks.gold_block, 0, true));

	private final BooleanSetting removeCaseOpening = new BooleanSetting()
		.name("CaseOpening-Benachrichtigungen entfernen")
		.icon("chest");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Chat aufräumen")
		.icon("speech_bubble")
		.subSettings(antiClearChat, removeSupremeSpaces, removeStreamerNotifications, removeMysteryMod, removeLuckyBlock, removeCaseOpening, news);


	private boolean isNews = false;

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		boolean isNewsLine = event.message.getFormattedText().equals("§r§f§m------------§r§8 [ §r§6News§r§8 ] §r§f§m------------§r");
		if (isNewsLine)
			isNews = !isNews;

		// News mode
		if (news.get() == NewsMode.NONE)
			event.setCanceled(isNews || isNewsLine);
		else if (news.get() == NewsMode.COMPACT)
			event.setCanceled(isNewsLine || (isNews && event.message.getFormattedText().trim().equals("§r§8\u00bb§r")));

		// Anti clear chat
		if (!event.isCanceled())
			event.setCanceled(antiClearChat.get() && event.message.getFormattedText().replaceAll("§.", "").trim().isEmpty());

		// remove supreme spaces
		if (!event.isCanceled())
			event.setCanceled(removeSupremeSpaces.get() && event.message.getFormattedText().trim().equals("§r§8\u00bb§r"));

		// remove streamer
		if (!event.isCanceled())
			event.setCanceled(removeStreamerNotifications.get() && event.message.getFormattedText().startsWith("§r§8[§6Streamer§8]"));

		// Remove MysteryMod download notification
		if (!event.isCanceled())
			event.setCanceled(removeMysteryMod.get() && MYSTERY_MOD_DOWNLOAD_NOTIFICATION.contains(event.message.getFormattedText()));

		// remove luckyblock
		if (!event.isCanceled())
			event.setCanceled(removeLuckyBlock.get() && event.message.getFormattedText().startsWith("§r§8[§r§e§lLu§r§6§lck§r§e§lyB§r§6§llo§r§e§lck§r§8]"));

		// remove case opening
		if (!event.isCanceled())
			event.setCanceled(removeCaseOpening.get() && event.message.getFormattedText().startsWith("§r§8[§r§bCase§r§fOpening§r§8]"));
	}

	private enum NewsMode {

		NORMAL("Normal"), COMPACT("Kompakt"), NONE("Versteckt");

		private final String name;
		NewsMode(String name) {
			this.name = name;
		}
	}

}
