/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.laby4.money;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.modules.Laby4Module;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.v1_8_9.features.modules.laby4.money.Received.moneyReceived;
import static dev.l3g7.griefer_utils.v1_8_9.features.modules.laby4.money.Spent.moneySpent;
import static java.math.BigDecimal.ZERO;
import static net.labymod.api.Textures.SpriteCommon.TRASH;

@Singleton
public class Earned extends Laby4Module {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Verdient")
		.description("Zeigt dir, wie viel Geld du seit deinem Minecraft-Start verdient hast.")
		.icon("coin_pile")
		.subSettings(ButtonSetting.create()
			.name("Zurücksetzen")
			.description("Setzt das eingenommene und das ausgegebene Geld zurück.")
			.icon("arrow_circle")
			.buttonIcon(TRASH)
			.callback(() -> {
				moneyReceived = ZERO;
				moneySpent = ZERO;
			}));

	@Override
	public Object getValue() {
		return DECIMAL_FORMAT_98.format(moneyReceived.subtract(moneySpent)) + "$";
	}

}
