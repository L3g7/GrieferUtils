/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.balance_stats;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.gui.griefer_games.BetterJobExchange;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.getNextServerRestart;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static java.math.BigDecimal.ZERO;

@Singleton
public class Received extends SimpleWidget {

	static BigDecimal moneyReceived = ZERO;
	private static boolean initialized = false; // NOTE cleanup
	private long nextReset = -1;

	private final SwitchSetting resetSetting = SwitchSetting.create()
		.name("Automatisch zurücksetzen")
		.description("Ob automatisch um 04:00 das eingenommene Geld zurückgesetzt werden soll.")
		.icon("hourglass")
		.callback(b -> {
			if (!initialized)
				return;

			if (!b)
				nextReset = -1;
			else
				nextReset = getNextServerRestart();
			Config.set("modules.money.data." + mc().getSession().getProfile().getId() + ".next_reset", new JsonPrimitive(nextReset));
			Config.save();
		});

	private static final SwitchSetting resetAfterRestart = SwitchSetting.create()
		.name("Nach Neustart zurücksetzen")
		.description("Ob nach einem Minecraft-Neustart das eingenommene Geld zurückgesetzt werden soll.")
		.icon("hourglass")
		.callback(shouldReset -> {
			if (!initialized)
				return;

			if (shouldReset)
				Config.set("modules.money.data." + mc().getSession().getProfile().getId() + ".received", new JsonPrimitive(ZERO));
			else
				Config.set("modules.money.data." + mc().getSession().getProfile().getId() + ".received", new JsonPrimitive(moneyReceived));
			Config.save();
		});

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Eingenommen")
		.description("Zeigt dir, wie viel Geld du seit deinem Minecraft-Start eingenommen hast.")
		.icon("high_res/wallets/ingoing")
		.subSettings(resetSetting, resetAfterRestart,
			ButtonSetting.create()
				.name("Zurücksetzen")
				.description("Setzt das eingenommene Geld zurück.")
				.icon("loop")
				.buttonIcon("loop")
				.callback(() -> setBalance(ZERO)),
			ButtonSetting.create()
				.name("Alles zurücksetzen")
				.description("Setzt das eingenommene und das ausgegebene Geld zurück.")
				.icon("loop")
				.buttonIcon("loop")
				.callback(() -> setBalance(Spent.setBalance(ZERO)))
		);

	@Override
	public String getValue() {
		return DECIMAL_FORMAT_98.format(moneyReceived) + "$";
	}

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = PAYMENT_RECEIVE_PATTERN.matcher(event.message.getFormattedText());
		if (!matcher.matches()) {
			matcher = JOB_SELL_PATTERN.matcher(event.message.getFormattedText());
			if (!matcher.matches())
				return;
		}

		setBalance(moneyReceived.add(new BigDecimal(matcher.group("amount").replace(",", ""))));
	}

	@EventListener(triggerWhenDisabled = true)
	public void onTick(ClientTickEvent tickEvent) {
		if (nextReset != -1 && System.currentTimeMillis() > nextReset) {
			nextReset = getNextServerRestart();
			Config.set("modules.money.data." + mc().getSession().getProfile().getId() + ".next_reset", new JsonPrimitive(nextReset));
			setBalance(ZERO);
			Config.save();
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void loadBalance(GrieferGamesJoinEvent ignored) {
		String path = "modules.money.data." + mc().getSession().getProfile().getId() + ".";

		if (Config.has(path + "received") && !resetAfterRestart.get())
			setBalance(Config.get(path + "received").getAsBigDecimal());
		if (Config.has(path + "next_reset")) {
			nextReset = Config.get(path + "next_reset").getAsLong();
			resetSetting.set(nextReset != -1);
		}

		initialized = true;
	}

	// Job cancelation
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
		int money = offer.getLeft() * offer.getRight();

		String feeLine = ItemUtil.getLoreAtIndex(stack, 5);
		feeLine = feeLine.substring("§cAnfallende Gebühr: §4".length(), feeLine.length() - 1 /* remove % */);
		double feePercentage = Double.parseDouble(feeLine) / 100;

		int fee = (int) Math.floor(feePercentage * (double) money);
		setBalance(moneyReceived.add(new BigDecimal(money - fee)));
	}

	protected static BigDecimal setBalance(BigDecimal newValue) {
		moneyReceived = newValue;
		if (!resetAfterRestart.get()) {
			Config.set("modules.money.data." + mc().getSession().getProfile().getId() + ".received", new JsonPrimitive(moneyReceived));
			Config.save();
		}
		return newValue;
	}

}
