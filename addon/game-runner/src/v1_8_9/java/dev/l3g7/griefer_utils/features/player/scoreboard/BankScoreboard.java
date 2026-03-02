/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.scoreboard;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.player.scoreboard.ScoreboardHandler.LineProvider;

@Singleton
public class BankScoreboard extends Feature implements LineProvider {

	private static long bankBalance = -1;

	public static long getBankBalance() {
		return bankBalance;
	}

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Bankguthaben im Scoreboard")
		.description("Fügt das Bankguthaben im Scoreboard hinzu.")
		.icon("bank")
		.callback(ScoreboardHandler::update);

	@EventListener(triggerWhenDisabled = true, priority = Priority.HIGH)
	public void onMMCustomPayload(MysteryModPayloadEvent event) {
		if (event.channel.equals("bank")) {
			bankBalance = event.payload.getAsJsonObject().get("amount").getAsLong();
			updateValue();
		}
	}

	@Override
	public boolean shouldHide(String key) {
		return key.equals("bank") && !isEnabled();
	}

	@Override
	public void createLine() {
		updateTeam("color_6", " ", "");
		updateTeam("bank_title", "§7ᐅ §3§l" + "Bankguth", "§3§l" + "aben");
		updateValue();
	}

	private void updateValue() {
		String value = bankBalance == -1 ? "?" : Constants.DECIMAL_FORMAT_98.format(bankBalance) + "$";
		updateTeam("bank_value", value, "");
	}

}