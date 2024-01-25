/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings;

import dev.l3g7.griefer_utils.core.auto_update.ReleaseInfo;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.FeatureCategory;
import dev.l3g7.griefer_utils.features.uncategorized.BugReporter;
import dev.l3g7.griefer_utils.misc.badges.Badges;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.init.Blocks;

import static dev.l3g7.griefer_utils.core.auto_update.ReleaseInfo.ReleaseChannel.STABLE;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Changelog.changelog;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Credits.credits;

@Singleton
@FeatureCategory
public class Settings extends Feature {

	@MainElement(configureSubSettings = false)
	private final CategorySetting element = new CategorySetting()
		.name("§yEinstellungen")
		.icon("cog")
		.subSettings(credits, changelog, new HeaderSetting(), Badges.enabled, autoUpdateEnabled, BugReporter.enabled);


	// Settings for AutoUpdater are here because the AutoUpdater class isn't affected by updates
	public static final BooleanSetting showUpdateScreen = new BooleanSetting()
		.name("Update-Screen anzeigen")
		.description("Ob ein Update-Screen angezeigt werden soll, wenn GrieferUtils geupdatet wurde.")
		.config("settings.auto_update.show_screen")
		.icon(ItemUtil.createItem(Blocks.stained_glass_pane, 0, true))
		.defaultValue(true);

	public static final DropDownSetting<ReleaseInfo.ReleaseChannel> releaseChannel = new DropDownSetting<>(ReleaseInfo.ReleaseChannel.class)
		.name("Version")
		.description("Ob auf die neuste stabile oder die Beta-Version geupdatet werden soll.")
		.config("settings.auto_update.release_channel")
		.icon("file")
		.defaultValue(STABLE);

	public static final BooleanSetting autoUpdateEnabled = new BooleanSetting()
		.name("Automatisch updaten")
		.description("Updatet GrieferUtils automatisch auf die neuste Version.")
		.config("settings.auto_update.enabled")
		.icon("arrow_circle")
		.defaultValue(true)
		.subSettings(showUpdateScreen, releaseChannel);

}