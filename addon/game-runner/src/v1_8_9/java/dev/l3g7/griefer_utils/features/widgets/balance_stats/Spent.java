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
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;

import java.math.BigDecimal;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.PAYMENT_SEND_PATTERN;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.getNextServerRestart;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.features.widgets.balance_stats.Earned.getResetIcon;
import static java.math.BigDecimal.ZERO;

@Singleton
public class Spent extends SimpleWidget {

	static BigDecimal moneySpent = BigDecimal.ZERO;
	private static boolean initialized = false; // NOTE cleanup
	private long nextReset = -1;

	private final SwitchSetting resetSetting = SwitchSetting.create()
		.name("Automatisch zurücksetzen")
		.description("Ob automatisch um 04:00 das ausgegebene Geld zurückgesetzt werden soll.")
		.icon("labymod_3/use_default_settings")
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
		.description("Ob nach einem Minecraft-Neustart das ausgegebene Geld zurückgesetzt werden soll.")
		.icon("labymod_3/use_default_settings")
		.callback(shouldReset -> {
			if (!initialized)
				return;

			if (shouldReset)
				Config.set("modules.money.data." + mc().getSession().getProfile().getId() + ".spent", new JsonPrimitive(ZERO));
			else
				Config.set("modules.money.data." + mc().getSession().getProfile().getId() + ".spent", new JsonPrimitive(moneySpent));
			Config.save();
		});

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Ausgegeben")
		.description("Zeigt dir, wie viel Geld du seit deinem Minecraft-Start ausgegeben hast.")
		.icon("wallets/outgoing")
		.subSettings(resetSetting, resetAfterRestart,
			ButtonSetting.create()
				.name("Zurücksetzen")
				.description("Setzt das ausgegebene Geld zurück.")
				.icon("arrow_circle")
				.buttonIcon(getResetIcon())
				.callback(() -> setBalance(ZERO)),
			ButtonSetting.create()
				.name("Alles zurücksetzen")
				.description("Setzt das eingenommene und das ausgegebene Geld zurück.")
				.icon("arrow_circle") // FIXME icon is ugly
				.buttonIcon(getResetIcon())
				.callback(() -> setBalance(Received.setBalance(ZERO)))
		);

	@Override
	public String getValue() {
		return DECIMAL_FORMAT_98.format(moneySpent) + "$";
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = PAYMENT_SEND_PATTERN.matcher(event.message.getFormattedText());
		if (matcher.matches())
			setBalance(moneySpent.add(new BigDecimal(matcher.group("amount").replace(",", ""))));
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

		if (Config.has(path + "spent") && !resetAfterRestart.get())
			setBalance(Config.get(path + "spent").getAsBigDecimal());
		if (Config.has(path + "next_reset")) {
			nextReset = Config.get(path + "next_reset").getAsLong();
			resetSetting.set(nextReset != -1);
		}

		initialized = true;
	}

	protected static BigDecimal setBalance(BigDecimal newValue) {
		moneySpent = newValue;
		// Save balance along with player uuid so no problems occur when using multiple accounts
		if (!resetAfterRestart.get()) {
			Config.set("modules.money.data." + mc().getSession().getProfile().getId() + ".spent", new JsonPrimitive(moneySpent));
			Config.save();
		}
		return newValue;
	}

}
