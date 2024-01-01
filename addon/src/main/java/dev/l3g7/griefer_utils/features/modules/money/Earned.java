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

package dev.l3g7.griefer_utils.features.modules.money;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import net.labymod.main.LabyMod;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.ControlElement.IconData;

import java.math.BigDecimal;

@Singleton
public class Earned extends Module {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Verdient")
		.description("Zeigt dir, wie viel Geld du seit Minecraft-Start verdient hast.")
		.icon("coin_pile")
		.subSettings(new SmallButtonSetting()
			.name("Zurücksetzen")
			.description("Setzt das eingenommene und das ausgegebene Geld zurück.")
			.icon("arrow_circle")
			.buttonIcon(new IconData(ModTextures.BUTTON_TRASH))
			.callback(() -> {
				Received.moneyReceived = BigDecimal.ZERO;
				Spent.moneySpent = BigDecimal.ZERO;
			}));

	@Override
	public String[] getValues() {
		return new String[]{Constants.DECIMAL_FORMAT_98.format(Received.moneyReceived.subtract(Spent.moneySpent)) + "$"};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"0$"};
	}

	@Override
	public boolean isShown() {
		return !LabyMod.getInstance().isInGame() || ServerCheck.isOnGrieferGames();
	}

}