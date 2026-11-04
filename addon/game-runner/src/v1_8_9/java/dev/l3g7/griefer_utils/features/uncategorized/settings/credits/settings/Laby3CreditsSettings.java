/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings.credits.settings;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.util.render.AsyncSkullRenderer;
import dev.l3g7.griefer_utils.features.uncategorized.settings.credits.Credits;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.CategorySettingImpl;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.HeaderSettingImpl;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.ModColor;

import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class Laby3CreditsSettings implements CreditsSettings {

	@Override
	public BaseSetting<?> createTextSetting(String... lines) {
		return new TextSetting(lines);
	}

	@Override
	public void addTeam(List<BaseSetting<?>> elements) {
		((CategorySettingImpl) Credits.credits).getSubSettings().getElements().addAll(10, c(elements));
	}

	@Override
	public BaseSetting<?> createCookieSetting() {
		return new CookieSetting("com.github.l3g73:freecookies", "Stellt gratis Kekse bereit: Klicke hier", "Cookie License 4.2");
	}

	@Override
	public BaseSetting<?> createUserSetting() {
		return new UserSetting();
	}

	private static class TextSetting extends HeaderSettingImpl {

		private final String[] lines;

		public TextSetting(String... lines) {
			super();
			this.lines = lines;
			entryHeight(lines.length * 10 + 3);
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			LabyMod.getInstance().getDrawUtils().drawRectangle(x - 50, y, maxX + 50, maxY, ModColor.toRGB(80, 80, 80, 60));

			int drawY = y + 2;
			for (String line : lines) {
				LabyMod.getInstance().getDrawUtils().drawString(line, x - 48, drawY);
				drawY += 10;
			}
		}


	}

	private static class UserSetting extends ControlElement implements Laby3Setting<UserSetting, Object> {

		private final ExtendedStorage<Object> storage = new ExtendedStorage<>(e -> JsonNull.INSTANCE, e -> NULL, NULL);

		UserSetting() {
			super("Vielen Dank für das Nutzen von GrieferUtils!", new IconData());
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			AsyncSkullRenderer.renderPlayerSkull(x + 3, y + 2);
		}

		@Override
		public ExtendedStorage<Object> getStorage() {
			return storage;
		}

	}

	public static class CookieSetting extends TextSetting {

		public CookieSetting(String... text) {
			super(text);
		}

		@Override
		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			CreditsSettings.giveCookie();
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			mouseOver = mouseX >= x + 135 && mouseX <= x + 155 && mouseY >= y + 12 && mouseY <= y + 20;
		}

	}

}
