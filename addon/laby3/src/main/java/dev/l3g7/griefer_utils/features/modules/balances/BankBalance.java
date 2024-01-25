/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules.balances;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.features.player.scoreboard.BankScoreboard;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;

@Singleton
public class BankBalance extends Module {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
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
