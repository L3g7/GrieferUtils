/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.laby4.money;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.modules.Laby4Module;

import java.math.BigDecimal;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.getNextServerRestart;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static java.math.BigDecimal.ZERO;
import static net.labymod.api.Textures.SpriteCommon.TRASH;

@Singleton
@ExclusiveTo(LABY_4)
public class Spent extends Laby4Module {

	static BigDecimal moneySpent = BigDecimal.ZERO;
	private long nextReset = -1;

	private final SwitchSetting resetSetting = SwitchSetting.create()
		.name("Automatisch zurücksetzen")
		.description("Ob automatisch um 04:00 das ausgegebene Geld zurückgesetzt werden soll.")
		.icon("labymod_3/use_default_settings")
		.config("modules.money_spent.automatically_reset")
		.callback(b -> {
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
		.config("modules.money_spent.reset_after_restart")
		.callback(shouldReset -> {
			if (shouldReset)
				Config.set("modules.money.balances." + mc().getSession().getProfile().getId() + ".spent", new JsonPrimitive(ZERO));
			else
				Config.set("modules.money.balances." + mc().getSession().getProfile().getId() + ".spent", new JsonPrimitive(moneySpent));
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
				.buttonIcon(TRASH)
				.callback(() -> setBalance(ZERO)),
			ButtonSetting.create()
				.name("Alles zurücksetzen")
				.description("Setzt das eingenommene und das ausgegebene Geld zurück.")
				.icon("arrow_circle") // FIXME icon is ugly
				.buttonIcon(TRASH)
				.callback(() -> setBalance(Received.setBalance(ZERO)))
		);

	@Override
	public Object getValue() {
		return DECIMAL_FORMAT_98.format(moneySpent) + "$";
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = Constants.PAYMENT_SEND_PATTERN.matcher(event.message.getFormattedText());
		if (matcher.matches())
			setBalance(moneySpent.add(new BigDecimal(matcher.group("amount").replace(",", ""))));
	}

	@EventListener(triggerWhenDisabled = true)
	public void onTick(TickEvent.ClientTickEvent tickEvent) {
		if (nextReset != -1 && System.currentTimeMillis() > nextReset ) {
			nextReset = getNextServerRestart();
			Config.set("modules.money.data." + mc().getSession().getProfile().getId() + ".next_reset", new JsonPrimitive(nextReset));
			setBalance(ZERO);
			Config.save();
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void loadBalance(GrieferGamesJoinEvent ignored) {
		String path = "modules.money.balances." + mc().getSession().getProfile().getId() + ".";

		if (Config.has(path + "spent") && !resetAfterRestart.get())
			setBalance(BigDecimal.valueOf(Config.get(path + "spent").getAsLong()));
		if (Config.has(path + "next_reset")) {
			nextReset = Config.get(path + "next_reset").getAsLong();
			resetSetting.set(nextReset != -1);
		}
	}

	protected static BigDecimal setBalance(BigDecimal newValue) {
		moneySpent = newValue;
		// Save balance along with player uuid so no problems occur when using multiple accounts
		if (!resetAfterRestart.get()) {
			Config.set("modules.money.balances." + mc().getSession().getProfile().getId() + ".spent", new JsonPrimitive(moneySpent));
			Config.save();
		}
		return newValue;
	}

}
