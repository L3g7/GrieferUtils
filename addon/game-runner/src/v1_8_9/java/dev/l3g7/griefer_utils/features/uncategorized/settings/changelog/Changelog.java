/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings.changelog;

import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.StaticApiRequest.StaticApiData.ChangelogEntry;
import dev.l3g7.griefer_utils.core.auto_update.AutoUpdater;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import net.labymod.api.Textures;
import net.labymod.main.ModTextures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.misc.VersionComparator.VERSION_COMPARATOR;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class Changelog {

	public Map<String, ChangelogEntry> changelogs;

	public static final CategorySetting changelog = CategorySetting.create()
		.name("§eChangelog")
		.description("§eVerbindet...")
		.icon("white_scroll")
		.disable()
		.subSettings();

	@OnStartupComplete
	public void onEnable() {
		if (!AutoUpdater.hasUpdated || !ConfigPatcher.versionChanged || changelogs == null)
			return;

		String version = LabyBridge.labyBridge.addonVersion();
		if (!changelogs.containsKey(version)) {
			BugReporter.reportError(new Throwable("Could not find changelog for " + version));
			return;
		}

		mc().displayGuiScreen(new GuiChangelog(true, changelogs.get(version), version));
	}

	@EventListener
	private void onStaticData(StaticDataReceiveEvent event) {
		List<BaseSetting<?>> entries = new ArrayList<>();

		changelogs = event.data.changelog.merged;

		// Populate settings
		for (Entry<String, ChangelogEntry> entry : changelogs.entrySet()) {
			Function<ButtonSetting, ButtonSetting> addIconFunc = LABY_4.isActive() ? this::addIconLaby4 : this::addIconLaby3;
			entries.add(addIconFunc.apply(ButtonSetting.create()
				.name(entry.getKey())
				.icon(entry.getValue().beta ? "scroll" : "white_scroll")
				.callback(() -> mc().displayGuiScreen(new GuiChangelog(false, entry.getValue(), entry.getKey())))));
		}

		// Unlock changelog setting
		entries.sort(Comparator.comparing(BaseSetting::name, VERSION_COMPARATOR));
		mc().addScheduledTask(() -> {
			changelog.subSettings(entries);

			changelog.name("Changelog")
				.description("Was sich in den einzelnen Updates von GrieferUtils verändert hat.")
				.enable();
		});
	}

	private ButtonSetting addIconLaby4(ButtonSetting button) { // TODO refactor
		return button.buttonIcon(Textures.SpriteCommon.SETTINGS);
	}

	private ButtonSetting addIconLaby3(ButtonSetting button) {
		return button.buttonIcon(ModTextures.BUTTON_ADVANCED);
	}

}