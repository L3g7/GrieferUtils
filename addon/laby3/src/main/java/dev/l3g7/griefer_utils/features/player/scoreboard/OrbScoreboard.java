/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.scoreboard;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.modules.orb_stats.OrbBalance;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Singleton
public class OrbScoreboard extends ScoreboardHandler.ScoreboardMod {

	public static final DecimalFormat DECIMAL_FORMAT_3 = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.GERMAN));

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Orbguthaben im Scoreboard")
		.description("Fügt das Orbguthaben im Scoreboard hinzu.")
		.icon("orb");

	public OrbScoreboard() {
		super("Orbguthaben", 0);
	}

	@Override
	protected String getValue() {
		long balance = OrbBalance.getBalance();
		return balance == -1 ? "?" : DECIMAL_FORMAT_3.format(balance);
	}

}