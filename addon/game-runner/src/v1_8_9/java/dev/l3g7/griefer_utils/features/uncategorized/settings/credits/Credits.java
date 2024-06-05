/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings.credits;

import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.credits.bridge.CreditsBridge.creditsBridge;
import static dev.l3g7.griefer_utils.core.misc.badges.BadgeManagerBridge.badgeManager;

public class Credits {

	public static final CategorySetting credits = CategorySetting.create()
		.name("Credits")
		.icon("labymod_3/multiplayer")
		.description("Das Team hinter GrieferUtils, sowie Copyright und Lizenzen.")
		.subSettings(
			HeaderSetting.create().entryHeight(5),
			creditsBridge.createIconSetting("Lizensiert unter Apache License 2.0", "scroll"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Entwickler"),
			creditsBridge.createIconSetting("L3g7", "l3g7"),
			creditsBridge.createIconSetting("L3g73", "l3g7"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Special Thanks"),
			creditsBridge.createIconSetting("TuxFRI", "tuxfri"),
			creditsBridge.createIconSetting("CobbleShop", "cobbleshop"),
			creditsBridge.createIconSetting("Pleezon", "pleezon"),
			creditsBridge.createIconSetting("Frreiheit", "frreiheit"),
			creditsBridge.createIconSetting("Griefer.Info", "griefer_info"),
			creditsBridge.createIconSetting("verbvllert_", "verbvllert_"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Bild-Credits"),
			ButtonSetting.create()
				.name("Credits öffnen")
				.icon("white_scroll")
				.buttonIcon("open_link")
				.callback(() -> labyBridge.openWebsite("https://grieferutils.l3g7.dev/image_credits")),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Code-Credits"),
			creditsBridge.createTextSetting("core.misc.BufferedImageLuminanceSource", "Umwandlung von Bildern in Licht-Bitmaps", "Aus com.google.zxing", "© 2009 ZXing authors", "Apache License 2.0"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Bibliotheken"),
			creditsBridge.createTextSetting("com.github.gatooooooo:ForgeGradle", "Fork von ForgeGradle für Gradle 6", "LPGL-2.1"),
			creditsBridge.createTextSetting("com.github.xcfrg:mixingradle", "Fork von MixinGradle für ForgeGradle 2.1", "MIT"),
			creditsBridge.createTextSetting("de.undercouch.download", "Integration der Mods in Gradle", "Apache License 2.0"),
			creditsBridge.createCookieLib(),
			creditsBridge.createTextSetting("org.mariuszgromada.math:MathParser.org-mXparser", "Gleichungsberechnung für Rechner", "eigener Open-Source-Lizenz (Dual)"),
			creditsBridge.createTextSetting("com.google.zxing:core", "QR-Code-Leser für QR-Code Scanner", "Apache License 2.0"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Und Du <3"),
			creditsBridge.createUserSetting(),
			HeaderSetting.create().entryHeight(22)
		);

	public static void addTeam() {
		List<String> supporter = new ArrayList<>();
		List<BaseSetting<?>> elements = new ArrayList<>();

		if (badgeManager.isSpecial("75c4a4bd-2dcf-46a2-b8f1-e5f44ce120db"))
			supporter.add("MoosLeitung");
		if (badgeManager.isSpecial("bc1f3d61-0878-4006-ba46-fb479fc37a1e"))
			supporter.add("0001EnderGirlLP");

		if (!supporter.isEmpty()) {
			elements.add(HeaderSetting.create("Supporter"));

			for (String sup : supporter)
				elements.add(creditsBridge.createIconSetting(sup, sup.toLowerCase()));

			elements.add(HeaderSetting.create().entryHeight(11));
		}

		creditsBridge.addTeam(elements);
	}

}
