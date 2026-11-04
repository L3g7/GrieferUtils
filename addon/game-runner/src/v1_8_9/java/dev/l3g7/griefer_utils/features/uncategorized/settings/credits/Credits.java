/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings.credits;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;
import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;

import java.util.*;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.LOWEST;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.credits.settings.CreditsSettings.creditsSettings;

public class Credits {

	public static final CategorySetting credits = CategorySetting.create()
		.name("Credits")
		.icon("players")
		.description("Das Team hinter GrieferUtils, sowie Copyright und Lizenzen.")
		.subSettings(filterNulls(
			createPadding(5),
			createIconSetting("Lizensiert unter Apache License 2.0", "../../scroll"),
			createPadding(11),

			HeaderSetting.create("Entwickler"),
			createIconSetting("L3g7", "l3g7"),
			createIconSetting("L3g73", "l3g7"),
			createPadding(11),

			HeaderSetting.create("Special Thanks"),
			createIconSetting("TuxFRI", "tuxfri"),
			createIconSetting("CobbleShop", "cobbleshop"),
			createIconSetting("Pleezon", "pleezon"),
			createIconSetting("verbvllert_", "verbvllert_"),
			createIconSetting("Griefer.Info", "griefer_info"),
			createIconSetting("CommunityRadar", "community_radar"),
			createPadding(11),

			HeaderSetting.create("Bild-Credits"),
			ButtonSetting.create()
				.name("Credits öffnen")
				.icon("scroll")
				.buttonIcon("open_link")
				.callback(() -> labyBridge.openWebsite("https://grieferutils.l3g7.dev/image_credits")),
			createPadding(11),

			HeaderSetting.create("Code-Credits"),
			createCodeLicense("core.misc.BufferedImageLuminanceSource", "Umwandlung von Bildern in Licht-Bitmaps", "Aus com.google.zxing", "© 2009 ZXing authors", "Apache License 2.0"),
			createCodeLicense("core.api.misc.NTP$NTPClient", "Zeit-Synchronisierung", "https://support.ntp.org/Support/JavaSntpClient", "© 2004 Adam Buckley (rev. 12)", "GPLv3"),
			createPadding(11),

			HeaderSetting.create("Bibliotheken"),
			createLibraryLicense("com.github.gatooooooo:ForgeGradle", "Fork von ForgeGradle für Gradle 6", "LPGL-2.1"),
			createLibraryLicense("com.github.xcfrg:mixingradle", "Fork von MixinGradle für ForgeGradle 2.1", "MIT"),
			createLibraryLicense("de.undercouch.download", "Integration der Mods in Gradle", "Apache License 2.0"),
			creditsSettings.createCookieSetting(),
			createLibraryLicense("org.mariuszgromada.math:MathParser.org-mXparser", "Gleichungsberechnung für Rechner", "eigener Open-Source-Lizenz (Dual)"),
			createLibraryLicense("com.google.zxing:core", "QR-Code-Leser für QR-Code Scanner", "Apache License 2.0"),
			createPadding(11),

			HeaderSetting.create("Und Du <3"),
			creditsSettings.createUserSetting(),
			createPadding(22)
		));

	private static BaseSetting<?> createLibraryLicense(String mavenCoords, String description, String license) {
		return creditsSettings.createTextSetting(
			"  - " + mavenCoords,
			"  - " + description,
			"  - Lizenziert unter " + license
		);
	}

	private static BaseSetting<?> createCodeLicense(String path, String description, String source, String copyright, String license) {
		return creditsSettings.createTextSetting(
			"  - " + path,
			"  - " + description,
			"  - " + source,
			"  - " + copyright,
			"  - Lizenziert unter " + license
		);
	}

	private static BaseSetting<?> createIconSetting(String displayName, String icon) {
		return CategorySetting.create()
			.name(displayName)
			.icon("high_res/credits/" + icon);
	}

	private static BaseSetting<?> createPadding(int height) {
		if (LABY_3.isActive())
			return HeaderSetting.create().entryHeight(height);
		else
			return null;
	}


	@EventListener(priority = LOWEST)
	private static void initTeam(StaticDataReceiveEvent event) {
		List<String> supporter = new ArrayList<>();
		List<BaseSetting<?>> elements = new ArrayList<>();

		if (Badges.getBadge(UUID.fromString("bc1f3d61-0878-4006-ba46-fb479fc37a1e")).isPresent())
			supporter.add("0001EnderGirlLP");
		if (Badges.getBadge(UUID.fromString("b45c62ef-fa60-4bc0-87bb-3a929ae8c7aa")).isPresent())
			supporter.add("Frreiheit");
		if (Badges.getBadge(UUID.fromString("75c4a4bd-2dcf-46a2-b8f1-e5f44ce120db")).isPresent())
			supporter.add("MoosLeitung");
		if (Badges.getBadge(UUID.fromString("39184854-ad19-4ea8-a797-5b7778a640a5")).isPresent())
			supporter.add("PoliceModzXD");

		if (!supporter.isEmpty()) {
			elements.add(HeaderSetting.create("Supporter"));

			for (String sup : supporter)
				elements.add(createIconSetting(sup, sup.toLowerCase()));

			BaseSetting<?> padding = createPadding(11);
			if (padding != null)
				elements.add(padding);
		}

		creditsSettings.addTeam(elements);
	}

	private static BaseSetting<?>[] filterNulls(BaseSetting<?>... settings) {
		return Arrays.stream(settings)
			.filter(Objects::nonNull)
			.toArray(BaseSetting[]::new);
	}

}
