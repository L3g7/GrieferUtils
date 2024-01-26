/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.settings;

import dev.l3g7.griefer_utils.api.util.Util;
import dev.l3g7.griefer_utils.laby4.settings.types.CategorySettingImpl;
import dev.l3g7.griefer_utils.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;

public class Credits {

	public static final CategorySetting credits = CategorySetting.create()
		.name("Credits")
		.icon("labymod:settings/tabs/multiplayer")
		.description("Das Team hinter GrieferUtils, sowie Copyright und Lizenzen.")
		.subSettings(
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
				.buttonIcon("griefer_utils/icons/open_link.png")
				.callback(() -> Util.openWebsite("https://grieferutils.l3g7.dev/image_credits")),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Code-Credits"),
			createTextSetting("core.misc.BufferedImageLuminanceSource", "Umwandlung von Bildern in Licht-Bitmaps", "Aus com.google.zxing", "© 2009 ZXing authors", "Apache License 2.0"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Bibliotheken"),
			createTextSetting("com.github.gatooooooo:ForgeGradle", "Fork von ForgeGradle für Gradle 6", "LPGL-2.1"),
			HeaderSetting.create().entryHeight(5),
			createTextSetting("com.github.xcfrg:mixingradle", "Fork von MixinGradle für ForgeGradle 2.1", "MIT"),
			HeaderSetting.create().entryHeight(5),
			createTextSetting("de.undercouch.download", "Integration der Mods in Gradle", "Apache License 2.0"),
			HeaderSetting.create().entryHeight(5),
			new CookieSetting("com.github.l3g73:freecookies", "Stellt gratis Kekse bereit: Klicke hier", "Cookie License 4.2"),
			HeaderSetting.create().entryHeight(5),
			createTextSetting("org.mariuszgromada.math:MathParser.org-mXparser", "Gleichungsberechnung für Rechner", "eigener Open-Source-Lizenz (Dual)"),
			HeaderSetting.create().entryHeight(5),
			createTextSetting("com.google.zxing:core", "QR-Code-Leser für QR-Code Scanner", "Apache License 2.0"),
			HeaderSetting.create().entryHeight(11),

			HeaderSetting.create("Und Du <3"),
			createUserSetting(),
			HeaderSetting.create().entryHeight(22)
		);

	/*
	TODO:
	public static void addTeam() {
		List<String> supporter = new ArrayList<>();
		List<SettingsElement> elements = new ArrayList<>();

		if (GrieferUtilsUserManager.isSpecial("75c4a4bd-2dcf-46a2-b8f1-e5f44ce120db"))
			supporter.add("MoosLeitung");
		if (GrieferUtilsUserManager.isSpecial("bc1f3d61-0878-4006-ba46-fb479fc37a1e"))
			supporter.add("0001EnderGirlLP");

		if (!supporter.isEmpty()) {
			elements.add(Settings.createHeader("Supporter"));

			for (String sup : supporter)
				elements.add(createIconSetting(sup, sup.toLowerCase()));

			elements.add(Settings.header().entryHeight(11));
		}

		credits.getSubSettings().getElements().addAll(10, elements);
	}*/

	private static CategorySetting createIconSetting(String displayName, String icon) {
		return CategorySetting.create()
			.name(displayName)
			.icon("credits/" + icon);
	}

	private static CategorySetting createUserSetting() {
		return CategorySetting.create()
			.name("Vielen Dank für das Nutzen von GrieferUtils!");
		/*TODO
		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			AsyncSkullRenderer.renderPlayerSkull(x + 3, y + 2);
		}
	*/
	}

	private static HeaderSetting createTextSetting(String... text) {

		for (int i = 1; i < text.length - 1; i++)
			text[i] = "  - " + text[i];

		int i = text.length - 1;
		text[i] = "  - Lizenziert unter " + text[i];

		return HeaderSetting.createText(text);
	}

	public static class CookieSetting extends CategorySettingImpl { // TODO: TextSetting
		private CookieSetting(String... text) {
		}
/*

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
*/
	}

}
