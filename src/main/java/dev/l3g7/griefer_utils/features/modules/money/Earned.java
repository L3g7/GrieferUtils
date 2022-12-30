/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.ButtonSetting;
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.misc.ServerCheck;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.math.BigDecimal;
import java.util.List;

@Singleton
public class Earned extends Module {

	public Earned() {
		super("Verdient", "Zeigt dir, wie viel Geld du seit Minecraft-Start verdient hast", "earned", new IconData("griefer_utils/icons/coin_pile.png"));
	}

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

	@Override
	public void fillSubSettings(List<SettingsElement> list) {
		list.add(new ButtonSetting().name("Zurücksetzen").callback(() -> {
			Received.moneyReceived = BigDecimal.ZERO;
			Spent.moneySpent = BigDecimal.ZERO;
		}));
	}

}