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

package dev.l3g7.griefer_utils.features.modules.laby3.money;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.item.recraft.laby3.SmallButtonSetting;
import dev.l3g7.griefer_utils.features.modules.Laby3Module;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;

import java.math.BigDecimal;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

@Singleton
@ExclusiveTo(LABY_3)
public class Earned extends Laby3Module {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Verdient")
		.description("Zeigt dir, wie viel Geld du seit deinem Minecraft-Start verdient hast.")
		.icon("coin_pile")
		.subSettings();

	private Earned() {
		((SettingsElement) enabled).getSubSettings().add(getResetAllButton());
	}

	public static SmallButtonSetting getResetAllButton() {
		SmallButtonSetting sbs = new SmallButtonSetting(new IconData("griefer_utils/arrow_circle.png"));
		sbs.setDisplayName("Zurücksetzen");
		sbs.setDescriptionText("Setzt das eingenommene und das ausgegebene Geld zurück.");
		sbs.buttonIcon(new IconData(ModTextures.BUTTON_TRASH));
		sbs.buttonCallback(() -> {
			Received.moneyReceived = BigDecimal.ZERO;
			Spent.moneySpent = BigDecimal.ZERO;
		});
		return sbs;
	}

	@Override
	public String[] getValues() {
		return new String[]{Constants.DECIMAL_FORMAT_98.format(Received.moneyReceived.subtract(Spent.moneySpent)) + "$"};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"0$"};
	}

}