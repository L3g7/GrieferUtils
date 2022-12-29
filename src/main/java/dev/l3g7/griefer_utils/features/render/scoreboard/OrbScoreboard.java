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

package dev.l3g7.griefer_utils.features.render.scoreboard;

import dev.l3g7.griefer_utils.features.modules.OrbBalance;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.Material;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Singleton
public class OrbScoreboard extends ScoreboardHandler.ScoreboardMod {

	public static final DecimalFormat DECIMAL_FORMAT_3 = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.GERMAN));

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Orbguthaben im Scoreboard")
		.icon(Material.EXP_BOTTLE);

	public OrbScoreboard() {
		super("Orbguthaben", 0);
	}

	@Override
	protected String getValue() {
		long balance = OrbBalance.getBalance();
		return balance == -1 ? "?" : DECIMAL_FORMAT_3.format(balance);
	}

}