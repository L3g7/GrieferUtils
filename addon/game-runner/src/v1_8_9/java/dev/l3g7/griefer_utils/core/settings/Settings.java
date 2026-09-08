/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.misc.player_resolver.PlayerListEntry;
import dev.l3g7.griefer_utils.core.settings.types.*;
import dev.l3g7.griefer_utils.core.settings.types.list.EntryAddSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.ListEntry;
import dev.l3g7.griefer_utils.core.settings.types.list.ListSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.StringListEntry;
import dev.l3g7.griefer_utils.features.Feature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;

@Bridged
public interface Settings {

	Settings settings = FileProvider.getBridge(Settings.class);

	CategorySetting createCategorySetting();

	HeaderSetting createHeaderSetting(String name);

	HeaderSetting createHeaderSettingWithRows(String... rows);

	SwitchSetting createSwitchSetting();

	SliderSetting createSliderSetting();

	StringSetting createStringSetting();

	NumberSetting createNumberSetting();

	KeySetting createKeySetting();

	ButtonSetting createButtonSetting();

	CitybuildSetting createCitybuildSetting();

	<E extends Enum<E> & Named> DropDownSetting<E> createDropDownSetting(Class<E> enumClass);

	<E extends ListEntry<E>> ListSetting<E> createListSetting(Class<E> type);

	ListSetting<PlayerListEntry> createPlayerListSetting();

	ListSetting<StringListEntry> createStringListSetting();

	EntryAddSetting createEntryAddSetting();

	static void buildMainPage(List<BaseSetting<?>> settings, Object registry) {
		List<GUIEntry> entries = new ArrayList<>(Feature.getCategories());
		Feature.getFeatures().forEach(entries::add);

		entries.stream()
			.sorted(Comparator.comparing(GUIEntry::name, SettingLoader::compareNames))
			.forEach(e -> e.addToParent(settings));

		settings.add(HeaderSetting.create());

		// Add uncategorized features
		Feature.getUncategorized().stream()
			.sorted(Comparator.comparing(BaseSetting::name, SettingLoader::compareNames))
			.forEach(settings::add);

		settings.add(HeaderSetting.create());

		// Wiki link
		settings.add(ButtonSetting.create()
			.name("Wiki")
			.description("Detailierte Erklärungen und Anleitungen für alle Features.")
			.icon("open_book")
			.buttonIcon("open_book_outline")
			.callback(() -> labyBridge.openWebsite("https://grieferutils.wiki")));

		// Ko-fi link
		settings.add(ButtonSetting.create()
			.name("Entwickler unterstützen")
			.description("Wenn dir das Addon gefällt kannst du hier das Entwickler-Team dahinter unterstützen §c❤")
			.icon("ko_fi")
			.buttonIcon("ko_fi_outline")
			.callback(() -> labyBridge.openWebsite("https://ko-fi.com/l3g7_3")));

		// Discord link
		settings.add(ButtonSetting.create()
			.name("Discord")
			.description("Vorschläge, Bugs und Support.")
			.icon("discord")
			.buttonIcon("discord_clyde")
			.callback(() -> labyBridge.openWebsite("https://grieferutils.l3g7.dev/discord")));

		// Initialize settings
		for (BaseSetting<?> s : settings)
			s.create(registry);
	}

}
