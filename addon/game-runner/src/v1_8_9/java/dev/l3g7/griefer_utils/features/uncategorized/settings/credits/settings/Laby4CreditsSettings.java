/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings.credits.settings;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.CategorySettingImpl;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.event.ClickEvent;
import net.labymod.api.configuration.settings.type.AbstractSetting;

import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.credits.Credits.credits;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class Laby4CreditsSettings implements CreditsSettings {

	public BaseSetting<?> createTextSetting(String... lines) {
		return CategorySetting.create()
			.name(String.join("\n", lines));
	}

	@Override
	public void addTeam(List<BaseSetting<?>> settings) {
		for (int i = 0; i < settings.size(); i++) {
			BaseSetting<?> setting = settings.get(i);
			setting.create(credits);
			credits.addSetting(4 + i, setting);
			((AbstractSetting) setting).initialize();
		}
	}

	@Override
	public BaseSetting<?> createCookieSetting() {
		return new CookieSettingImpl();
	}

	@Override
	public BaseSetting<?> createUserSetting() {
		return CategorySetting.create()
			.name("Vielen Dank für das Nutzen von GrieferUtils!")
			.<CategorySettingImpl>into()
			.icon(Icons.OWN_SKULL);
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

	}

	@EventListener(priority = Priority.HIGHEST)
	public void onMessageSend(MessageSendEvent event) {
		if (event.message.equals("/gu:y6Y7s8G88J1OLHwhMTEQYPbJ")) {
			event.cancel();
			CreditsSettings.giveCookie();
		}
	}

}
