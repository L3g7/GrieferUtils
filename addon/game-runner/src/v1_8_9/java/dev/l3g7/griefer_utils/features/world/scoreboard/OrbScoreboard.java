/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.scoreboard;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.griefergames.OrbBalanceUpdateEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.world.scoreboard.ScoreboardHandler.LineProvider;
import dev.l3g7.griefer_utils.features.widgets.orb_stats.OrbBalance;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Singleton
public class OrbScoreboard extends Feature implements LineProvider {

	public static final DecimalFormat DECIMAL_FORMAT_3 = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.GERMAN));

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Orbguthaben im Scoreboard")
		.description("Fügt das Orbguthaben im Scoreboard hinzu.")
		.icon("orb")
		.callback(ScoreboardHandler::update);

	@EventListener(triggerWhenDisabled = true)
	public void onMMCustomPayload(OrbBalanceUpdateEvent event) {
		updateValue();
	}

	@Override
	public boolean shouldHide(String key) {
		return key.equals("orb") && !isEnabled();
	}

	@Override
	public void createLine() {
		updateTeam("color_7", " ", "");
		updateTeam("orb_title", "§7ᐅ §3§l" + "Orbguth", "§3§l" + "aben");
		updateValue();
	}

	private void updateValue() {
		long balance = OrbBalance.get().getBalance();
		String value = balance == -1 ? "?" : DECIMAL_FORMAT_3.format(balance);
		updateTeam("orb_value", value, "");
	}

}
