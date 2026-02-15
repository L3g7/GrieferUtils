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
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.client.Minecraft;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.LOWEST;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.credits.bridge.CreditsBridge.creditsBridge;

public class Credits {

	public static final CategorySetting credits = CategorySetting.create()
		.name("Credits")
		.icon("XZRF:players")
		.description("Das Team hinter GrieferUtils, sowie Copyright und Lizenzen.")
		.subSettings(
			HeaderSetting.create().entryHeight(5),
			creditsBridge.createIconSetting("Lizensiert unter Apache License 2.0", "../../scroll"),
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
			creditsBridge.createIconSetting("verbvllert_", "verbvllert_"),
			creditsBridge.createIconSetting("Griefer.Info", "griefer_info"),
			creditsBridge.createIconSetting("CommunityRadar", "community_radar"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Bild-Credits"),
			ButtonSetting.create()
				.name("Credits öffnen")
				.icon("XZRF:scroll")
				.buttonIcon("XZRF:open_link")
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

	@EventListener(priority = LOWEST)
	private static void initTeam(StaticDataReceiveEvent event) {
		List<String> supporter = new ArrayList<>();
		List<BaseSetting<?>> elements = new ArrayList<>();

		if (Badges.getBadge(UUID.fromString("75c4a4bd-2dcf-46a2-b8f1-e5f44ce120db")).isPresent())
			supporter.add("MoosLeitung");
		if (Badges.getBadge(UUID.fromString("bc1f3d61-0878-4006-ba46-fb479fc37a1e")).isPresent())
			supporter.add("0001EnderGirlLP");

		if (!supporter.isEmpty()) {
			elements.add(HeaderSetting.create("Supporter"));

			for (String sup : supporter)
				elements.add(creditsBridge.createIconSetting(sup, sup.toLowerCase()));

			elements.add(HeaderSetting.create().entryHeight(11));
		}

		creditsBridge.addTeam(elements);
	}

	public static void giveCookie() {
		if (player() == null) {
			labyBridge.notify("§6Keks", "§eDu musst ingame sein!");
			return;
		}

		String nbt = "{id:\"minecraft:cookie\",Count:1b,tag:{display:{Lore:[\"\",\"§f§lGuten Appetit!\",\"§7Signiert von §aGrieferUtils §7am §e%s\"],Name:\"§6§lKeks\"}},Damage:0s}";
		nbt = String.format(nbt, new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
		boolean success = player().inventory.addItemStackToInventory(ItemUtil.fromNBT(nbt));
		labyBridge.notify("§6Keks", success ? "Guten Appetit!" : "§eDu musst Platz im Inventar haben!");
		Minecraft.getMinecraft().displayGuiScreen(null);
	}

}
