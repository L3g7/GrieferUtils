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

package dev.l3g7.griefer_utils.features.chat.chat_filter_templates;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.ingamechat.tabs.GuiChatFilter;
import net.labymod.ingamechat.tools.filter.Filters.Filter;

@Singleton
public class ChatFilterTemplates extends Feature {

	static final FilterTemplate[] TEMPLATES = new FilterTemplate[] {
		new FilterTemplate("Eingehende MSG").contains("-> mir]").containsNot("»"),
		new FilterTemplate("Ausgehende MSG").contains("[mir ->").containsNot("»"),
		new FilterTemplate("Globalchat").contains("@["),
		new FilterTemplate("Plotchat").contains("[Plot-Chat]").containsNot("»"),
		new FilterTemplate("Eingehende Zahlung").contains(" gegeben.").containsNot("»", "->", ":", "Du hast"),
		new FilterTemplate("Ausgehende Zahlung").contains(" gegeben.").containsNot("»", "->", ":", "[GrieferGames]", "hat dir"),
		new FilterTemplate("MobRemover").contains("[MobRemover]").containsNot("»", "->", ":"),
		new FilterTemplate("Clearlag").contains("auf dem Boden liegende Items entfernt!", "[GrieferGames] Warnung! Die auf dem Boden liegenden Items werden in").containsNot("»", "->", ":"),
		new FilterTemplate("Greeting").contains("[Greeting]").containsNot("»").highlight(255, 0, 0),
		new FilterTemplate("Farewell").contains("[Farewell]").containsNot("»").highlight(255, 0, 0),
		new FilterTemplate("GrieferUtils").contains("[GrieferUtils]").containsNot("»")
	};

	@MainElement
    private final BooleanSetting enabled = new BooleanSetting()
		.name("Filtervorlagen")
		.description("Fügt Vorlagen bei LabyMods Chatfiltern hinzu.")
		.icon("labymod:chat/filter");

    @EventListener
    public void onGuiOpen(GuiOpenEvent<GuiChatFilter> event) {
        if (event.gui.getClass() != GuiChatFilterWithTemplates.class)
            event.gui = new GuiChatFilterWithTemplates(Reflection.get(event.gui, "defaultInputFieldText"));
    }

	public static class FilterTemplate {

		public final String name;
		public String[] contains = new String[0];
		public String[] containsNot = new String[0];
		public boolean highlighting = false;
		public short red = 200;
		public short green = 200;
		public short blue = 50;

		private FilterTemplate(String name) {
			this.name = name;
		}

		public FilterTemplate contains(String... contains) {
			this.contains = contains;
			return this;
		}

		public FilterTemplate containsNot(String... containsNot) {
			this.containsNot = containsNot;
			return this;
		}

		public FilterTemplate highlight(int red, int green, int blue) {
			this.highlighting = true;
			this.red = (short) red;
			this.green = (short) green;
			this.blue = (short) blue;
			return this;
		}

		public Filter toFilter() {
			return new Filter(name, contains, containsNot, false, "note.harp", highlighting, red, green, blue, false, !highlighting, false, "Global");
		}

	}

}