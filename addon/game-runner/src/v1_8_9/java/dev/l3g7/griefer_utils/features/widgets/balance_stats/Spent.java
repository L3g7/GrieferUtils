/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.balance_stats;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import java.math.BigDecimal;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.PAYMENT_SEND_PATTERN;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class Spent extends BalanceStatWidget {

	public Spent() {
		super("Ausgegeben", "spent", "outgoing");
	}

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = PAYMENT_SEND_PATTERN.matcher(event.message.getFormattedText());
		if (matcher.matches())
			setBalance(money.add(new BigDecimal(matcher.group("amount").replace(",", ""))));
	}

	// Job creation
	@EventListener
	private void onWindowClick(WindowClickEvent event) {
		if (event.slotId != 29) // Accept button
			return;

		if (!event.windowTitle.startsWith("§aAuftrag erstellen"))
			return;

		if (!(mc().currentScreen instanceof GuiContainer gc))
			return; // Race condition :(

		ItemStack stack = gc.inventorySlots.inventorySlots.get(13).getStack();
		String totalPriceLore = ItemUtil.getLoreAtIndex(stack, 2);
		if (!totalPriceLore.startsWith("§7Gesamt: §e§l"))
			return;

		String price = totalPriceLore.substring("§7Gesamt: §e§l".length())
			.replaceAll("§.|\\$|,|\\.", "");

		setBalance(money.add(new BigDecimal(price)));
	}

}
