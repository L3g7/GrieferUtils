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
import dev.l3g7.griefer_utils.core.api.misc.primitives.containers.Lazy;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.SettingLoader;
import dev.l3g7.griefer_utils.core.settings.Settings;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.misc.badges.Badges.icon;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

@ExclusiveTo(LABY_3)
public class MainPage {

	public static final StringSetting filter = StringSetting.create()
		.name("Suche")
		.icon("magnifying_glass")
		.callback(MainPage::onSearch);

	private static final List<SettingsElement> searchableSettings = new ArrayList<>();

	private static Timer timer = new Timer();

	private static final Lazy<List<BaseSetting<?>>> settings = new Lazy<>(MainPage::collectSettings);

	private static List<BaseSetting<?>> collectSettings() {
		List<BaseSetting<?>> settings = new ArrayList<>(Arrays.asList(
			HeaderSetting.create("§r"),
			HeaderSetting.create("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
			HeaderSetting.create("§e§lStartseite").scale(.7),
			HeaderSetting.create("§r").scale(.4).entryHeight(10),
			filter,
			HeaderSetting.create("§r").scale(.4).entryHeight(10))
		);

		// Initialize search
		Feature.getFeatures()
			.forEach(feature -> {
				searchableSettings.add((SettingsElement) feature.getMainElement());

				((SettingsElement) feature.getMainElement()).getSubSettings().getElements().stream()
					.filter(e -> e instanceof BaseSetting<?>)
					.forEach(searchableSettings::add);
			});

		Feature.getFeatures().forEach(f ->
			((Laby3Setting<?, ?>) f.getMainElement()).getStorage().alias = f.getClass().getSimpleName());

		// Initialize settings
		Settings.buildMainPage(settings, null);
		searchableSettings.sort(Comparator.comparing(SettingsElement::getDisplayName, SettingLoader::compareNames));
		return settings;
	}

	public static List<BaseSetting<?>> getSettings() {
		return settings.get();
	}

	private static void onSearch() {
		TickScheduler.runNextRenderTick(() -> {
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

						icon = icon.equals("icon") ? filter.get() : "icon";
						filter.set("");
						labyBridge.notify("§aEaster Egg", "Easter Egg wurde " + (icon.equals("icon") ? "de" : "") + "aktiviert.");
						if (world() != null)
							MinecraftUtil.closeClientsideGUI();

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
				listedElementsStored.addAll(c(getSettings()));
				return;
			}

			int startIndex = 6;
			while (listedElementsStored.size() > startIndex)
				listedElementsStored.remove(startIndex);

			String needle = filter.get().toLowerCase();
			searchableSettings.stream()
				.filter(s -> s.getDisplayName().replaceAll("§.", "").toLowerCase().contains(needle)
					|| ((Laby3Setting<?, ?>) s).getStorage().alias.toLowerCase().contains(needle))
				.forEach(v -> listedElementsStored.add(c(v)));
		});
	}

	@EventListener
	private static void onGuiInit(GuiScreenEvent.GuiInitEvent event) {
		if (!(event.gui instanceof LabyModAddonsGui))
			return;

		filter.getStorage().value = "";

		Reflection.set(filter, "currentValue", "");
		ModTextField textField = Reflection.get(filter, "textField");
		textField.setText("");
		textField.setFocused(true);
	}

}
