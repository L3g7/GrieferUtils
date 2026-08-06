/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.balance_info;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.Option;
import dev.l3g7.griefer_utils.core.misc.griefer_games.Balances;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;

import java.math.BigDecimal;

@Singleton
public class CoinBalance extends SimpleWidget {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Kontostand")
		.description("Zeigt den Kontostand an.")
		.icon("coin");

	@Override
	public String getValue() {
		Option<BigDecimal> balance = Balances.getBalance();
		if (balance.isUnset())
			return "?";

		return Constants.DECIMAL_FORMAT_98.format(balance.get()) + "$";
	}

}
