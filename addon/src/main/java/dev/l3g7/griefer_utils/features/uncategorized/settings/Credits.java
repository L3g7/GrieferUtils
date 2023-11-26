/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings;

import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.misc.badges.GrieferUtilsUserManager;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.ModColor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

public class Credits {

	public static final CategorySetting credits = new CategorySetting()
		.name("Credits")
		.icon("labymod:settings/tabs/multiplayer")
		.description("Das Team hinter GrieferUtils, sowie Copyright und Lizenzen.")
		.subSettings(
			new IconSetting("Lizensiert unter Apache License 2.0", "scroll"),
			new HeaderSetting().entryHeight(11),

			new HeaderSetting("Entwickler"),
			new Credits.IconSetting("L3g7", "l3g7"),
			new Credits.IconSetting("L3g73", "l3g7"),
			new HeaderSetting().entryHeight(11),

			new HeaderSetting("Special Thanks"),
			new Credits.IconSetting("TuxFRI", "tuxfri"),
			new Credits.IconSetting("CobbleShop", "cobbleshop"),
			new Credits.IconSetting("Pleezon", "pleezon"),
			new HeaderSetting().entryHeight(11),

			new HeaderSetting("Bild-Credits"),
			new SmallButtonSetting()
				.name("Credits öffnen")
				.icon("white_scroll")
				.buttonIcon(new IconData("griefer_utils/icons/open_link.png"))
				.callback(() -> Util.openWebsite("https://grieferutils.l3g7.dev/image_credits")),
			new HeaderSetting().entryHeight(11),

			new HeaderSetting("Code-Credits"),
			new TextSetting("core.misc.BufferedImageLuminanceSource", "Umwandlung von Bildern in Licht-Bitmaps", "Aus com.google.zxing", "© 2009 ZXing authors", "Apache License 2.0"),
			new HeaderSetting().entryHeight(11),

			new HeaderSetting("Bibliotheken"),
			new TextSetting("com.github.gatooooooo:ForgeGradle", "Fork von ForgeGradle für Gradle 6", "LPGL-2.1"),
			new HeaderSetting().entryHeight(5),
			new TextSetting("com.github.xcfrg:mixingradle", "Fork von MixinGradle für ForgeGradle 2.1", "MIT"),
			new HeaderSetting().entryHeight(5),
			new TextSetting("de.undercouch.download", "Integration der Mods in Gradle", "Apache License 2.0"),
			new HeaderSetting().entryHeight(5),
			new CookieSetting("com.github.l3g73:freecookies", "Stellt gratis Kekse bereit: Klicke hier", "Cookie License 4.2"),
			new HeaderSetting().entryHeight(5),
			new TextSetting("org.mariuszgromada.math:MathParser.org-mXparser", "Gleichungsberechnung für Rechner", "eigener Open-Source-Lizenz (Dual)"),
			new HeaderSetting().entryHeight(5),
			new TextSetting("com.google.zxing:core", "QR-Code-Leser für QR-Code Scanner", "Apache License 2.0"),
			new HeaderSetting().entryHeight(11),

			new HeaderSetting("Und Du <3"),
			new UserSetting(),
			new HeaderSetting().entryHeight(22)
		);

	public static void addTeam() {
		List<String> supporter = new ArrayList<>();
		List<SettingsElement> elements = new ArrayList<>();

		if (GrieferUtilsUserManager.isSpecial("75c4a4bd-2dcf-46a2-b8f1-e5f44ce120db"))
			supporter.add("MoosLeitung");
		if (GrieferUtilsUserManager.isSpecial("bc1f3d61-0878-4006-ba46-fb479fc37a1e"))
			supporter.add("0001EnderGirlLP");

		if (!supporter.isEmpty()) {
			elements.add(new HeaderSetting("Supporter"));

			for (String sup : supporter)
				elements.add(new IconSetting(sup, sup.toLowerCase()));

			elements.add(new HeaderSetting().entryHeight(11));
		}

		credits.getSubSettings().getElements().addAll(10, elements);
	}

	private static class IconSetting extends ControlElement {

		public IconSetting(String displayName, String icon) {
			super(displayName, new IconData("griefer_utils/icons/credits/" + icon + ".png"));
		}

	}

	private static class UserSetting extends ControlElement {

		private UserSetting() {
			super("Vielen Dank für das Nutzen von GrieferUtils!", new IconData());
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			RenderUtil.renderPlayerSkull(x + 3, y + 2);
		}

	}

	private static class TextSetting extends HeaderSetting {

		private final String[] lines;

		private TextSetting(String... text) {
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
			drawUtils().drawRectangle(x - 50, y, maxX + 50, maxY, ModColor.toRGB(80, 80, 80, 60));

			int drawY = y + 2;
			for (String line : lines) {
				drawUtils().drawString(line, x - 48, drawY);
				drawY += 10;
			}
		}

	}

	public static class CookieSetting extends TextSetting {

		private CookieSetting(String... text) {
			super(text);
		}

		@Override
		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			if (player() == null) {
				displayAchievement("§6Keks", "§eDu musst ingame sein!");
				return;
			}

			String nbt = "{id:\"minecraft:cookie\",Count:1b,tag:{display:{Lore:[\"\",\"§f§lGuten Appetit!\",\"§7Signiert von §aGrieferUtils §7am §e%s\"],Name:\"§6§lKeks\"}},Damage:0s}";
			nbt = String.format(nbt, new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
			boolean success = player().inventory.addItemStackToInventory(ItemUtil.fromNBT(nbt));
			displayAchievement("§6Keks", success ? "Guten Appetit!" : "§eDu musst Platz im Inventar haben!");
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			mouseOver = mouseX >= x + 135 && mouseX <= x + 155 && mouseY >= y + 12 && mouseY <= y + 20;
		}

	}

}
