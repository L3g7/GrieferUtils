/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.settings.credits.bridge;

import dev.l3g7.griefer_utils.api.bridges.Bridge;
import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.v1_8_9.misc.SkullIcon;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingHeaderWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.AbstractSetting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.settings.credits.Credits.credits;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class Laby4Credits implements CreditsBridge {

	public CategorySetting createIconSetting(String displayName, String icon) {
		return CategorySetting.create()
			.name(displayName)
			.icon("credits/" + icon);
	}

	public BaseSetting<?> createTextSetting(String... text) {

		for (int i = 1; i < text.length - 1; i++)
			text[i] = "  - " + text[i];

		int i = text.length - 1;
		text[i] = "  - Lizenziert unter " + text[i];

		return CategorySetting.create()
			.name(String.join("\n", text));
	}

	@Override
	public void addTeam(List<BaseSetting<?>> elements) {
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

	@Override
	public BaseSetting<?> createCookieLib() {
		return createTextSetting("com.github.l3g73:freecookies", "Stellt gratis Kekse bereit: Klicke hier", "Cookie License 4.2");
	}

	@Override
	public BaseSetting<?> createUserSetting() {
		return CategorySetting.create()
			.name("Vielen Dank für das Nutzen von GrieferUtils!")
			.icon(SkullIcon.OWN);
	}

}
