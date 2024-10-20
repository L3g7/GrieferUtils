/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.misc.badges.laby3.GrieferUtilsGroup;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.*;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.SwitchSettingImpl;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

@ExclusiveTo(LABY_3)
public class MainPage {

	private static Timer timer = new Timer();

	public static final StringSetting filter = StringSetting.create()
		.name("Suche")
		.icon("magnifying_glass")
		.callback(MainPage::onSearch);

	private static final List<SettingsElement> searchableSettings = new ArrayList<>();
	private static List<BaseSetting<?>> settings = Collections.emptyList();

	public static List<BaseSetting<?>> collectSettings() {
		settings = new ArrayList<>(Arrays.asList(
			HeaderSetting.create("§r"),
			HeaderSetting.create("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
			HeaderSetting.create("§e§lStartseite").scale(.7),
			HeaderSetting.create("§r").scale(.4).entryHeight(10),
			filter,
			HeaderSetting.create("§r").scale(.4).entryHeight(10)));

		// Enable the feature category if one of its features gets enabled
		Feature.getFeatures()
			.sorted(Comparator.comparing(f -> f.getMainElement().name()))
			.forEach(feature -> {
				if (!feature.getClass().isAnnotationPresent(Feature.FeatureCategory.class)) {
					searchableSettings.add(c(feature.getMainElement()));
					return;
				}

				((SettingsElement) feature.getMainElement()).getSubSettings().getElements().stream()
					.filter(e -> e instanceof SwitchSetting || e instanceof NumberSetting || e instanceof CategorySetting)
					.forEachOrdered(searchableSettings::add);

				if (!(feature.getMainElement() instanceof SwitchSettingImpl main)) {
					return;
				}

				for (BaseSetting<?> element : main.getChildSettings()) {
					if (!(element instanceof SwitchSetting sub))
						continue;

					sub.callback(b -> {
						if (b)
							main.set(true);
					});
				}
			});

		// Add features to categories
		Feature.getFeatures()
			.sorted(Comparator.comparing(f -> f.getMainElement().name()))
			.forEach(Feature::addToCategory);

		// Add categories
		Feature.getCategories().stream()
			.sorted(Comparator.comparing(BaseSetting::name))
			.forEach(settings::add);

		settings.add(HeaderSetting.create());

		// Add uncategorized features
		Feature.getUncategorized().stream()
			.sorted(Comparator.comparing(BaseSetting::name))
			.forEach(settings::add);

		settings.add(HeaderSetting.create());

		// Wiki link
		settings.add(ButtonSetting.create()
			.name("Wiki").icon("open_book")
			.buttonIcon("open_book_outline")
			.callback(() -> labyBridge.openWebsite("https://grieferutils.wiki")));

		// Ko-fi link
		settings.add(ButtonSetting.create()
			.name("Entwickler unterstützen").icon("ko_fi")
			.description("Wenn dir das Addon gefällt kannst du hier das Entwickler-Team dahinter unterstützen §c❤")
			.buttonIcon("ko_fi_outline")
			.callback(() -> labyBridge.openWebsite("https://ko-fi.com/l3g7_3")));

		// Discord link
		settings.add(ButtonSetting.create()
			.name("Discord").icon("discord")
			.buttonIcon("discord_clyde")
			.callback(() -> labyBridge.openWebsite("https://grieferutils.l3g7.dev/discord")));

		// Create settings
		for (BaseSetting<?> setting : settings)
			setting.create(null);

		searchableSettings.sort(Comparator.comparing(SettingsElement::getDisplayName));

		return settings;
	}


	private static void onSearch() {
		TickScheduler.runAfterRenderTicks(() -> {
			if (!(mc().currentScreen instanceof LabyModAddonsGui))
				return;

			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = digest.digest(("griefer_utils_salt_" + filter.get()).getBytes(StandardCharsets.UTF_8));
			String hash = Base64.getEncoder().encodeToString(bytes);

			if (hash.equals("IBmzqW3cyeMT0Gj/VqDLhnOhI0Qdhx6FgqFsdLbvzGA=")) {
				timer = new Timer();
				timer.schedule(new TimerTask() {
					public void run() {
						if (!(mc().currentScreen instanceof LabyModAddonsGui))
							return;

						GrieferUtilsGroup.icon = GrieferUtilsGroup.icon.equals("icon") ? filter.get() : "icon";
						filter.set("");
						labyBridge.notify("§aEaster Egg", "Easter Egg wurde umgeschalten.");
						if (world() != null)
							mc().displayGuiScreen(null);

						timer = null;
					}
				}, 3179);
			} else if (timer != null) {
				timer.cancel();
				timer = null;
			}

			List<SettingsElement> listedElementsStored = Reflection.get(mc().currentScreen, "tempElementsStored");

			if (filter.get().isEmpty()) {
				listedElementsStored.clear();
				listedElementsStored.addAll(c(settings));
				return;
			}

			int startIndex = 6;
			while (listedElementsStored.size() > startIndex)
				listedElementsStored.remove(startIndex);

			searchableSettings.stream()
				.filter(s -> s.getDisplayName().replaceAll("§.", "").toLowerCase().contains(filter.get().toLowerCase()))
				.forEach(v -> listedElementsStored.add(c(v)));
		}, 1);
	}

	@EventListener
	private static void onGuiInit(GuiScreenEvent.GuiInitEvent event) {
		if (!(event.gui instanceof LabyModAddonsGui))
			return;

		filter.getStorage().value = "";

		Reflection.set(filter, "currentValue", "");
		ModTextField textField = Reflection.get(filter, "textField");
		textField.setText("");
		textField.setFocused(false);

		// TODO filter.unfocus(0, 0, 0);
	}

}
