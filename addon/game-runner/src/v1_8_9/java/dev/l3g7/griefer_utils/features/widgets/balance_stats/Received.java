/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.balance_stats;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.gui.griefer_games.BetterJobExchange;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import java.math.BigDecimal;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.JOB_SELL_PATTERN;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.PAYMENT_RECEIVE_PATTERN;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class Received extends BalanceStatWidget {

	public Received() {
		super("Eingenommen", "received", "ingoing");
	}

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = PAYMENT_RECEIVE_PATTERN.matcher(event.message.getFormattedText());
		if (!matcher.matches()) {
			matcher = JOB_SELL_PATTERN.matcher(event.message.getFormattedText());
			if (!matcher.matches())
				return;
		}

		setBalance(money.add(new BigDecimal(matcher.group("amount").replace(",", ""))));
	}

	// Job cancellation
	@EventListener
	private void onWindowClick(WindowClickEvent event) {
		if (event.slotId != 29) // Accept button
			return;

		if (!event.windowTitle.startsWith("§6Auftrag abbrechen"))
			return;

		if (!(mc().currentScreen instanceof GuiContainer gc))
			return; // Race condition :(

		ItemStack stack = gc.inventorySlots.inventorySlots.get(13).getStack();
		String line = ItemUtil.getLoreAtIndex(stack, 3);
		if (line.isEmpty())
			return;

		Pair<Integer, Integer> offer = BetterJobExchange.extractOffer(line);
		int money = offer.a * offer.b;

		String feeLine = ItemUtil.getLoreAtIndex(stack, 5);
		feeLine = feeLine.substring("§cAnfallende Gebühr: §4".length(), feeLine.length() - 1 /* remove % */);
		double feePercentage = Double.parseDouble(feeLine) / 100;

		int fee = (int) Math.floor(feePercentage * (double) money);
		setBalance(this.money.add(new BigDecimal(money - fee)));
	}

}
