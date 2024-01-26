/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.money;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Module;

import java.math.BigDecimal;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.getNextServerRestart;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static java.math.BigDecimal.ZERO;

@Singleton
public class Received extends Module {

	static BigDecimal moneyReceived = ZERO;
	private long nextReset = -1;

	private final SwitchSetting resetSetting = SwitchSetting.create()
		.name("Automatisch zurücksetzen")
		.description("Ob automatisch um 04:00 das eingenommene Geld zurückgesetzt werden soll.")
		.icon("labymod_3/use_default_settings")
		.config("modules.money_received.automatically_reset")
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
		.description("Ob nach einem Minecraft-Neustart das eingenommene Geld zurückgesetzt werden soll.")
		.icon("labymod_3/use_default_settings")
		.config("modules.money_received.reset_after_restart")
		.callback(shouldReset -> {
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
		.icon("wallets/ingoing")
		.subSettings(resetSetting, resetAfterRestart,
			ButtonSetting.create()
				.name("Zurücksetzen")
				.description("Setzt das eingenommene Geld zurück.")
				.icon("arrow_circle")
				.buttonIcon("labymod_3/trash")
				.callback(() -> setBalance(ZERO)),
			ButtonSetting.create()
				.name("Alles zurücksetzen")
				.description("Setzt das eingenommene und das ausgegebene Geld zurück.")
				.icon("arrow_circle")
				.buttonIcon("labymod_3/trash")
				.callback(() -> setBalance(Spent.setBalance(ZERO)))
		);

	@Override
	public String[] getValues() {
		return new String[]{Constants.DECIMAL_FORMAT_98.format(moneyReceived) + "$"};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"0$"};
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = Constants.PAYMENT_RECEIVE_PATTERN.matcher(event.message.getFormattedText());
		if (matcher.matches())
			setBalance(moneyReceived.add(new BigDecimal(matcher.group("amount").replace(",", ""))));
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
		String path = "modules.money.data." + mc().getSession().getProfile().getId() + ".";

		if (Config.has(path + "received") && !resetAfterRestart.get())
			setBalance(BigDecimal.valueOf(Config.get(path + "received").getAsLong()));
		if (Config.has(path + "next_reset")) {
			nextReset = Config.get(path + "next_reset").getAsLong();
			resetSetting.set(nextReset != -1);
		}
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