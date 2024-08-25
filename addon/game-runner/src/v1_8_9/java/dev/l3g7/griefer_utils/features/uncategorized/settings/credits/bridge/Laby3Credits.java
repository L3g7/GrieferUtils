/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings.credits.bridge;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.HeaderSettingImpl;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.render.AsyncSkullRenderer;
import dev.l3g7.griefer_utils.features.uncategorized.settings.credits.Credits;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.CategorySettingImpl;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.ModColor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class Laby3Credits implements CreditsBridge {

	@Override
	public BaseSetting<?> createIconSetting(String displayName, String icon) {
		return new IconSetting(displayName, icon);
	}

	@Override
	public BaseSetting<?> createTextSetting(String... text) {
		return new TextSetting(text);
	}

	@Override
	public void addTeam(List<BaseSetting<?>> elements) {
		((CategorySettingImpl) Credits.credits).getSubSettings().getElements().addAll(10, c(elements));
	}

	@Override
	public BaseSetting<?> createCookieLib() {
		return new CookieSetting("com.github.l3g73:freecookies", "Stellt gratis Kekse bereit: Klicke hier", "Cookie License 4.2");
	}

	@Override
	public BaseSetting<?> createUserSetting() {
		return new UserSetting();
	}

	private static class IconSetting extends ControlElement implements Laby3Setting<IconSetting, Object> {

		private final ExtendedStorage<Object> storage = new ExtendedStorage<>(e -> JsonNull.INSTANCE, e -> NULL, NULL);

		public IconSetting(String displayName, String icon) {
			super(displayName, new IconData("griefer_utils/icons/credits/" + icon + ".png"));
		}

		@Override
		public ExtendedStorage<Object> getStorage() {
			return storage;
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

	private static class TextSetting extends HeaderSettingImpl {

		private final String[] lines;

		public TextSetting(String... text) {
			super();
			lines = text;
			entryHeight(lines.length * 10 + 3);

			for (int i = 1; i < lines.length - 1; i++)
				lines[i] = "  - " + lines[i];

			int i = lines.length - 1;
			lines[i] = "  - Lizenziert unter " + lines[i];
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

	public static class CookieSetting extends TextSetting {

		public CookieSetting(String... text) {
			super(text);
		}

		@Override
		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			if (player() == null) {
				labyBridge.notify("§6Keks", "§eDu musst ingame sein!");
				return;
			}

			String nbt = "{id:\"minecraft:cookie\",Count:1b,tag:{display:{Lore:[\"\",\"§f§lGuten Appetit!\",\"§7Signiert von §aGrieferUtils §7am §e%s\"],Name:\"§6§lKeks\"}},Damage:0s}";
			nbt = String.format(nbt, new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
			boolean success = player().inventory.addItemStackToInventory(ItemUtil.fromNBT(nbt));
			labyBridge.notify("§6Keks", success ? "Guten Appetit!" : "§eDu musst Platz im Inventar haben!");
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			mouseOver = mouseX >= x + 135 && mouseX <= x + 155 && mouseY >= y + 12 && mouseY <= y + 20;
		}

	}

}
