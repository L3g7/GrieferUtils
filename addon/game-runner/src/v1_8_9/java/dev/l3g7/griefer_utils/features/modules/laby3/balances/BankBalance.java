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

package dev.l3g7.griefer_utils.features.modules.laby3.balances;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.modules.Laby3Module;
import dev.l3g7.griefer_utils.features.player.scoreboard.BankScoreboard;

@Singleton
public class BankBalance extends Laby3Module {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Bankguthaben")
		.description("Zeigt das Bankguthaben an.")
		.icon("bank");

	@Override
	public String[] getValues() {
		if (BankScoreboard.getBankBalance() == -1)
			return getDefaultValues();

		return new String[] {Constants.DECIMAL_FORMAT_98.format(BankScoreboard.getBankBalance()) + "$"};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[] {"?"};
	}

}
