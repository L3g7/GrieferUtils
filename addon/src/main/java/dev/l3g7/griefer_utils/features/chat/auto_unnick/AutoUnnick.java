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

package dev.l3g7.griefer_utils.features.chat.auto_unnick;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.event_bus.Priority;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.uncategorized.BugReporter;
import dev.l3g7.griefer_utils.misc.NameCache;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.IChatComponentUtil;
import net.labymod.utils.Material;
import net.minecraft.util.IChatComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class AutoUnnick extends Feature {

	private final BooleanSetting tab = new BooleanSetting()
		.name("In Tabliste")
		.description("Ob Spieler in der Tabliste entnickt werden sollen.")
		.icon("tab_list")
		.callback(TabListEvent::updatePlayerInfoList);

	private final BooleanSetting chat = new BooleanSetting()
		.name("In Chat")
		.description("Ob Spieler im Chat entnickt werden sollen.")
		.icon("speech_bubble")
		.defaultValue(true);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Automatisch entnicken")
		.description("Zeigt statt Nicks den echten Namen an.")
		.icon(Material.NAME_TAG)
		.callback(TabListEvent::updatePlayerInfoList)
		.subSettings(chat, tab);

	@Override
	public void init() {
		super.init();
		getCategory().getSetting().callback(TabListEvent::updatePlayerInfoList);
	}

	@EventListener(priority = Priority.HIGH)
	public void onTabListNameUpdate(TabListEvent.TabListNameUpdateEvent event) {
		if (!tab.get())
			return;

		String text = event.component.getUnformattedText();

		if (!text.contains("~"))
			return;

		String nickName = text.substring(text.indexOf('~'));
		String[] parts = event.component.getFormattedText().split(" ?§r§8§*l* ?\u2503 §r");

		if (parts.length != 2) {
			BugReporter.reportError(new Throwable(IChatComponent.Serializer.componentToJson(event.component) + " | " + event.profile));
			return;
		}

		IChatComponentUtil.setNameWithPrefix(event.component, nickName, NameCache.getName(nickName), new PrefixFinder(parts[0], parts[1]).getPrefix(), true);
	}

	@EventListener(priority = Priority.HIGH)
	public void onMessageModifyChat(MessageEvent.MessageModifyEvent event) {
		if (!chat.get())
			return;

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

		String realName = NameCache.getName(name);
		if (realName == null)
			return;

		for (Pattern pattern : Constants.MESSAGE_PATTERNS) {
			Matcher matcher = pattern.matcher(event.message.getFormattedText());

			if (matcher.matches()) {
				IChatComponentUtil.setNameWithPrefix(event.message, name, realName, new PrefixFinder(matcher.group("rank"), matcher.group("name")).getPrefix(), false);
				return;
			}
		}
	}

}
