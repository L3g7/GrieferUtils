/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.scoreboard;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.event_bus.Priority;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.event.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;

@Singleton
public class BankScoreboard extends ScoreboardHandler.ScoreboardMod {

	private static long bankBalance = -1;

	public static long getBankBalance() {
		return bankBalance;
	}

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Bankguthaben im Scoreboard")
		.description("Fügt das Bankguthaben im Scoreboard hinzu.")
		.icon("bank");

	public BankScoreboard() {
		super("Bankguthaben", 1);
	}

	@EventListener(triggerWhenDisabled = true, priority = Priority.HIGH)
	public void onMMCustomPayload(MysteryModPayloadEvent event) {
		if (event.channel.equals("bank"))
			bankBalance = event.payload.getAsJsonObject().get("amount").getAsLong();
	}

	@Override
	protected String getValue() {
		return bankBalance == -1 ? "?" : Constants.DECIMAL_FORMAT_98.format(bankBalance) + "$";
	}

}