/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings.credits.bridge;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.misc.SkullIcon;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.CategorySettingImpl;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.event.ClickEvent;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.AbstractSetting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.credits.Credits.credits;

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

	@Override
	public BaseSetting<?> createCookieLib() {
		return new CookieSettingImpl();
	}

	@Override
	public BaseSetting<?> createUserSetting() {
		return CategorySetting.create()
			.name("Vielen Dank für das Nutzen von GrieferUtils!")
			.icon(SkullIcon.OWN);
	}

	private static class CookieSettingImpl extends CategorySettingImpl {

		@Override
		public Component displayName() {
			return Component.text(String.join("\n",
					"com.github.l3g73:freecookies",
					"  - Stellt gratis Kekse bereit: Klicke hier",
					"  - Lizenziert unter Cookie License 4.2"))
				.clickEvent(ClickEvent.runCommand("/gu:y6Y7s8G88J1OLHwhMTEQYPbJ"));
		}

		@EventListener
		private void onMessageSend(MessageEvent.MessageSendEvent event) {
			if (!event.message.equals("/gu:y6Y7s8G88J1OLHwhMTEQYPbJ"))
				return;

			event.cancel();
			String nbt = "{id:\"minecraft:cookie\",Count:1b,tag:{display:{Lore:[\"\",\"§f§lGuten Appetit!\",\"§7Signiert von §aGrieferUtils §7am §e%s\"],Name:\"§6§lKeks\"}},Damage:0s}";
			nbt = String.format(nbt, new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
			boolean success = player().inventory.addItemStackToInventory(ItemUtil.fromNBT(nbt));
			labyBridge.notify("§6Keks", success ? "Guten Appetit!" : "§eDu musst Platz im Inventar haben!");
		}

	}

}
