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

package dev.l3g7.griefer_utils.features.chat.auto_unnick;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.NameCache;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.IChatComponentUtil;
import net.labymod.utils.Material;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.event.events.network.TabListEvent.updatePlayerInfoList;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.displayAchievement;

@Singleton
public class AutoUnnick extends Feature {

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
		getCategory().getSetting().addCallback(v -> updatePlayerInfoList());
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

		if (parts.length != 2) {
			System.err.println(IChatComponent.Serializer.componentToJson(event.component));
			System.out.println(event.profile);
			displayAchievement("§c§lFehler \u26A0", "§cBitte melde dich beim Team.");
			return;
		}

		IChatComponentUtil.setNameWithPrefix(event.component, nickName, NameCache.getName(nickName), new PrefixFinder(parts[0], parts[1]).getPrefix(), true);
	}

	@EventListener(priority = EventPriority.HIGH)
	public void onMessageModifyChat(MessageEvent.MessageModifyEvent event) {
		String text = event.message.getUnformattedText();

		if (!text.contains("\u2503") || !text.contains("~") || text.startsWith("@"))
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
				IChatComponentUtil.setNameWithPrefix(event.message, name, NameCache.getName(name), new PrefixFinder(matcher.group("rank"), matcher.group("name")).getPrefix(), true);
				return;
			}
		}
	}

}
