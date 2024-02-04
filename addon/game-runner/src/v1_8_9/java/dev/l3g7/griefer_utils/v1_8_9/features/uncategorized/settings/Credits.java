/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.settings;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.laby4.settings.OwnHeadIcon;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.v1_8_9.misc.badges.BadgeManager;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingHeaderWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.AbstractSetting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

public class Credits {

	public static final CategorySetting credits = CategorySetting.create()
		.name("Credits")
		.icon("labymod_3/multiplayer")
		.description("Das Team hinter GrieferUtils, sowie Copyright und Lizenzen.")
		.subSettings(
			HeaderSetting.create().entryHeight(5),
			createIconSetting("Lizensiert unter Apache License 2.0", "scroll"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Entwickler"),
			createIconSetting("L3g7", "l3g7"),
			createIconSetting("L3g73", "l3g7"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Special Thanks"),
			createIconSetting("TuxFRI", "tuxfri"),
			createIconSetting("CobbleShop", "cobbleshop"),
			createIconSetting("Pleezon", "pleezon"),
			createIconSetting("Frreiheit", "frreiheit"),
			createIconSetting("Griefer.Info", "griefer_info"),
			createIconSetting("verbvllert_", "verbvllert_"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Bild-Credits"),
			ButtonSetting.create()
				.name("Credits öffnen")
				.icon("white_scroll")
				.buttonIcon("open_link")
				.callback(() -> labyBridge.openWebsite("https://grieferutils.l3g7.dev/image_credits")),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Code-Credits"),
			createTextSetting("core.misc.BufferedImageLuminanceSource", "Umwandlung von Bildern in Licht-Bitmaps", "Aus com.google.zxing", "© 2009 ZXing authors", "Apache License 2.0"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Bibliotheken"),
			createTextSetting("com.github.gatooooooo:ForgeGradle", "Fork von ForgeGradle für Gradle 6", "LPGL-2.1"),
			createTextSetting("com.github.xcfrg:mixingradle", "Fork von MixinGradle für ForgeGradle 2.1", "MIT"),
			createTextSetting("de.undercouch.download", "Integration der Mods in Gradle", "Apache License 2.0"),
			createTextSetting("com.github.l3g73:freecookies", "Stellt gratis Kekse bereit: Klicke hier", "Cookie License 4.2"),
			createTextSetting("org.mariuszgromada.math:MathParser.org-mXparser", "Gleichungsberechnung für Rechner", "eigener Open-Source-Lizenz (Dual)"),
			createTextSetting("com.google.zxing:core", "QR-Code-Leser für QR-Code Scanner", "Apache License 2.0"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Und Du <3"),
			createUserSetting(),
			HeaderSetting.create().entryHeight(22)
		);

	public static void addTeam() {
		List<String> supporter = new ArrayList<>();
		List<BaseSetting<?>> elements = new ArrayList<>();

		if (BadgeManager.isSpecial("75c4a4bd-2dcf-46a2-b8f1-e5f44ce120db"))
			supporter.add("MoosLeitung");
		if (BadgeManager.isSpecial("bc1f3d61-0878-4006-ba46-fb479fc37a1e"))
			supporter.add("0001EnderGirlLP");

		if (!supporter.isEmpty()) {
			elements.add(HeaderSetting.create("Supporter"));

			for (String sup : supporter)
				elements.add(createIconSetting(sup, sup.toLowerCase()));

			elements.add(HeaderSetting.create().entryHeight(11));
		}

		List<AbstractSetting> settings = c(elements);
		Setting parent = c(credits);

		for (int i = 0; i < settings.size(); i++) {
			AbstractSetting setting = settings.get(i);
			setting.setParent(parent);
			credits.addSetting(7 + i, c(setting));
			if (parent.isInitialized())
				setting.initialize();
		}
	}

	private static CategorySetting createIconSetting(String displayName, String icon) {
		return CategorySetting.create()
			.name(displayName)
			.icon("credits/" + icon);
	}

	private static CategorySetting createUserSetting() {
		return CategorySetting.create()
			.name("Vielen Dank für das Nutzen von GrieferUtils!")
			.icon(new OwnHeadIcon());
	}

	private static BaseSetting<?> createTextSetting(String... text) {

		for (int i = 1; i < text.length - 1; i++)
			text[i] = "  - " + text[i];

		int i = text.length - 1;
		text[i] = "  - Lizenziert unter " + text[i];

		return CategorySetting.create()
			.name(String.join("\n", text));
	}

	@EventListener
	private static void onInit(SettingActivityInitEvent event) {
		if (event.holder() != credits)
			return;

		for (Widget child : event.settings().getChildren()) {
			if (child.actualWidget() instanceof SettingHeaderWidget header) {
				Component displayName = Reflection.get(header, "displayName");
				if (displayName instanceof TextComponent text && "  - Stellt gratis Kekse bereit: Klicke hier".equals(text.getText())) {
					child.setPressable(() -> {
						if (player() == null) {
							labyBridge.notify("§6Keks", "§eDu musst ingame sein!");
							return;
						}

						String nbt = "{id:\"minecraft:cookie\",Count:1b,tag:{display:{Lore:[\"\",\"§f§lGuten Appetit!\",\"§7Signiert von §aGrieferUtils §7am §e%s\"],Name:\"§6§lKeks\"}},Damage:0s}";
						nbt = String.format(nbt, new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
						boolean success = player().inventory.addItemStackToInventory(ItemUtil.fromNBT(nbt));
						labyBridge.notify("§6Keks", success ? "Guten Appetit!" : "§eDu musst Platz im Inventar haben!");
					});
				}
			}
		}
	}

}
