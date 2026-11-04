/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.balance_stats;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.DECIMAL_FORMAT_98;
import static java.math.BigDecimal.ZERO;

@Singleton
public class Earned extends SimpleWidget {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Verdient")
		.description("Zeigt dir, wie viel Geld du seit deinem Minecraft-Start verdient hast.")
		.icon("coin")
		.subSettings(ButtonSetting.create()
			.name("Zurücksetzen")
			.description("Setzt das eingenommene und das ausgegebene Geld zurück.")
			.icon("loop")
			.buttonIcon("loop")
			.callback(() -> {
				get(Received.class).setBalance(ZERO);
				get(Spent.class).setBalance(ZERO);
			}));

	@Override
	public String getValue() {
		return DECIMAL_FORMAT_98.format(get(Received.class).getBalance().subtract(get(Spent.class).getBalance())) + "$";
	}

}
