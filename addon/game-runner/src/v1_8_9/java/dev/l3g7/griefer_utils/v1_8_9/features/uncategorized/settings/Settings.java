/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.settings;

import dev.l3g7.griefer_utils.api.BugReporter;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.auto_update.ReleaseInfo.ReleaseChannel;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.Feature.FeatureCategory;
import dev.l3g7.griefer_utils.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.misc.badges.Badges;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.init.Blocks;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.auto_update.ReleaseInfo.ReleaseChannel.BETA;
import static dev.l3g7.griefer_utils.auto_update.ReleaseInfo.ReleaseChannel.STABLE;
import static dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.settings.Changelog.changelog;
import static dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.settings.Credits.credits;

@Singleton
@FeatureCategory
public class Settings extends Feature {

	@MainElement(configureSubSettings = false)
	private final CategorySetting element = CategorySetting.create()
		.name("§yEinstellungen")
		.icon("cog")
		.subSettings(credits, changelog, HeaderSetting.create(), Badges.enabled, MainMenuSkull.enabled, autoUpdateEnabled, BugReporter.enabled);

	// Settings for AutoUpdater are here because the AutoUpdater class isn't affected by updates
	public static final SwitchSetting showUpdateScreen = SwitchSetting.create()
		.name("Update-Screen anzeigen")
		.description("Ob ein Update-Screen angezeigt werden soll, wenn GrieferUtils geupdatet wurde.")
		.config("settings.auto_update.show_screen")
		.icon(ItemUtil.createItem(Blocks.stained_glass_pane, 0, true))
		.defaultValue(true);

	public static final DropDownSetting<ReleaseChannel> releaseChannel = DropDownSetting.create(ReleaseChannel.class)
		.name("Version")
		.description("Ob auf die neuste stabile oder die Beta-Version geupdatet werden soll.")
		.config("settings.auto_update.release_channel")
		.icon("file")
		.defaultValue(STABLE);

	static {
		releaseChannel.callback(v -> {
			if (v == STABLE) {
				labyBridge.notify("§e§lFehler ⚠", "§eLabyMod 4 ist in der Beta!");
				releaseChannel.set(BETA);
			}
		});
	}

	public static final SwitchSetting autoUpdateEnabled = SwitchSetting.create()
		.name("Automatisch updaten")
		.description("Updatet GrieferUtils automatisch auf die neuste Version.")
		.config("settings.auto_update.enabled")
		.icon("arrow_circle")
		.defaultValue(true)
		.subSettings(showUpdateScreen, releaseChannel);

}