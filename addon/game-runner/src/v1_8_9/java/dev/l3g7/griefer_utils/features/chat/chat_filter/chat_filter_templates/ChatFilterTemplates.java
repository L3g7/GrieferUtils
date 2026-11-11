/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_templates;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;

@Singleton
public class ChatFilterTemplates extends Feature {

	public static final FilterTemplate[] TEMPLATES = new FilterTemplate[]{
		new FilterTemplate("Eingehende MSG", "chat").contains("-> mir]").containsNot("»"),
		new FilterTemplate("Ausgehende MSG", "book_and_quill").contains("[mir ->").containsNot("»"),
		new FilterTemplate("Globalchat", "earth").contains("@["),
		new FilterTemplate("Plotchat", "chat_orange").contains("[Plot-Chat]").containsNot("»"),
		new FilterTemplate("Eingehende Zahlung", "high_res/wallets/ingoing").contains(" gegeben.").containsNot("»", "->", "Du hast"),
		new FilterTemplate("Ausgehende Zahlung", "high_res/wallets/outgoing").contains(" gegeben.").containsNot("»", "->", "[GrieferGames]", "hat dir"),
		new FilterTemplate("Bank", "bank").contains("[Bank]").containsNot("»", "->"),
		new FilterTemplate("MobRemover", "crossed_out_zombie").contains("[MobRemover]").containsNot("»", "->"),
		new FilterTemplate("Clearlag", "crossed_out_gold_ingot").contains("auf dem Boden liegende Items entfernt!", "[GrieferGames] Warnung! Die auf dem Boden liegenden Items werden in").containsNot("»", "->"),
		new FilterTemplate("Greeting", "wooden_board").contains("[Greeting]").containsNot("»").highlight(255, 0, 0),
		new FilterTemplate("Farewell", "blackboard").contains("[Farewell]").containsNot("»").highlight(255, 0, 0),
		new FilterTemplate("GrieferUtils", "high_res/icon").contains("[GrieferUtils]").containsNot("»")
	};

	@MainElement
	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Filtervorlagen")
		.description("Fügt Vorlagen bei LabyMods Chatfiltern hinzu.")
		.icon("enchanted_book");

	public static ChatFilterTemplates get() {
		return get(ChatFilterTemplates.class);
	}

	public static class FilterTemplate {

		public final String name;
		public final String icon;
		public String[] contains = new String[0];
		public String[] containsNot = new String[0];
		public boolean highlighting = false;
		public short red = 200;
		public short green = 200;
		public short blue = 50;

		private FilterTemplate(String name, String icon) {
			this.name = name;
			this.icon = icon;
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

	}

}