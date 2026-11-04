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
import dev.l3g7.griefer_utils.core.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;

import java.math.BigDecimal;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.getNextServerRestart;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static java.math.BigDecimal.ZERO;

public abstract class BalanceStatWidget extends SimpleWidget {

	protected BigDecimal money = ZERO;
	private boolean initialized = false;
	private long nextReset = -1;

	private final String configKey;

	private final SwitchSetting resetSetting;
	private final SwitchSetting resetAfterRestart;

	@MainElement
	private final SwitchSetting enabled;

	public BalanceStatWidget(String verb, String configKey, String icon) {
		String pastParticipleVerb = verb.toLowerCase() + "e";

		this.configKey = configKey;

		resetSetting = SwitchSetting.create()
			.name("Automatisch zurücksetzen")
			.description("Ob automatisch um 04:00 das " + pastParticipleVerb + " Geld zurückgesetzt werden soll.")
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

		resetAfterRestart = SwitchSetting.create()
			.name("Nach Neustart zurücksetzen")
			.description("Ob nach einem Minecraft-Neustart das " + pastParticipleVerb + " Geld zurückgesetzt werden soll.")
			.icon("hourglass")
			.callback(shouldReset -> {
				if (!initialized)
					return;

				if (shouldReset)
					Config.set("modules.money.data." + mc().getSession().getProfile().getId() + "." + configKey, new JsonPrimitive(ZERO));
				else
					Config.set("modules.money.data." + mc().getSession().getProfile().getId() + "." + configKey, new JsonPrimitive(money));
				Config.save();
			});

		enabled = SwitchSetting.create()
			.name(verb)
			.description("Zeigt dir, wie viel Geld du seit deinem Minecraft-Start " + verb.toLowerCase() + " hast.")
			.icon("high_res/wallets/" + icon)
			.subSettings(resetSetting, resetAfterRestart,
				ButtonSetting.create()
					.name("Zurücksetzen")
					.description("Setzt das " + pastParticipleVerb + " Geld zurück.")
					.icon("loop")
					.buttonIcon("loop")
					.callback(() -> setBalance(ZERO)),
				ButtonSetting.create()
					.name("Alles zurücksetzen")
					.description("Setzt das eingenommene und das ausgegebene Geld zurück.")
					.icon("loop")
					.buttonIcon("loop")
					.callback(() -> setBalance(get(getClass()).setBalance(ZERO)))
			);
	}

	private String configKey(String subPath) {
		// Save balance along with player uuid so no problems occur when using multiple accounts
		return "modules.money.data." + mc().getSession().getProfile().getId() + "." + subPath;
	}

	protected BigDecimal getBalance() {
		return money;
	}

	protected BigDecimal setBalance(BigDecimal newValue) {
		money = newValue;
		if (!resetAfterRestart.get()) {
			Config.set(configKey(configKey), new JsonPrimitive(money));
			Config.save();
		}
		return newValue;
	}

	@Override
	public String getValue() {
		return DECIMAL_FORMAT_98.format(money) + "$";
	}

	@EventListener(triggerWhenDisabled = true)
	public void onTick(ClientTickEvent tickEvent) {
		if (nextReset != -1 && System.currentTimeMillis() > nextReset) {
			nextReset = getNextServerRestart();
			Config.set(configKey("next_reset"), new JsonPrimitive(nextReset));
			setBalance(ZERO);
			Config.save();
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void loadBalance(GrieferGamesJoinEvent ignored) {
		if (Config.has(configKey(configKey)) && !resetAfterRestart.get())
			setBalance(Config.get(configKey(configKey)).getAsBigDecimal());
		if (Config.has(configKey("next_reset"))) {
			nextReset = Config.get(configKey("next_reset")).getAsLong();
			resetSetting.set(nextReset != -1);
		}

		initialized = true;
	}

}
