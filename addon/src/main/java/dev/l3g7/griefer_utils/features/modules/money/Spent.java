/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.modules.money;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.ControlElement.IconData;
import net.minecraft.util.IChatComponent;

import java.math.BigDecimal;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.getNextServerRestart;
import static java.math.BigDecimal.ZERO;

@Singleton
public class Spent extends Module {

	static BigDecimal moneySpent = BigDecimal.ZERO;
	private long nextReset = -1;

	private final BooleanSetting resetSetting = new BooleanSetting()
		.name("Automatisch zurücksetzen")
		.description("Ob automatisch um 04:00 das ausgegebene Geld zurückgesetzt werden soll.")
		.icon(ModTextures.SETTINGS_DEFAULT_USE_DEFAULT_SETTINGS)
		.config("modules.money_spent.automatically_reset")
		.callback(b -> {
			if (!b)
				nextReset = -1;
			else
				nextReset = getNextServerRestart();
			Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".next_reset", new JsonPrimitive(nextReset));
			Config.save();
		});

	private static final BooleanSetting resetAfterRestart = new BooleanSetting()
		.name("Nach Neustart zurücksetzen")
		.description("Ob nach einem Minecraft-Neustart das ausgegebene Geld zurückgesetzt werden soll.")
		.icon(ModTextures.SETTINGS_DEFAULT_USE_DEFAULT_SETTINGS)
		.config("modules.money_spent.reset_after_restart")
		.callback(shouldReset -> {
			if (shouldReset)
				Config.set("modules.money.balances." + mc.getSession().getProfile().getId() + ".spent", new JsonPrimitive(ZERO));
			else
				Config.set("modules.money.balances." + mc.getSession().getProfile().getId() + ".spent", new JsonPrimitive(moneySpent));
			Config.save();
		});

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Ausgegeben")
		.description("Zeigt dir, wie viel Geld du seit Minecraft-Start ausgegeben hast.")
		.icon("wallet_outgoing")
		.subSettings(resetSetting, resetAfterRestart,
			new SmallButtonSetting()
				.name("Zurücksetzen")
				.description("Setzt das ausgegebene Geld zurück.")
				.icon("arrow_circle")
				.buttonIcon(new IconData(ModTextures.BUTTON_TRASH))
				.callback(() -> setBalance(ZERO, "single reset")),
			new SmallButtonSetting()
				.name("Alles zurücksetzen")
				.description("Setzt das eingenommene und das ausgegebene Geld zurück.")
				.icon("arrow_circle")
				.buttonIcon(new IconData(ModTextures.BUTTON_TRASH))
				.callback(() -> setBalance(Received.setBalance(ZERO, "multi reset from spent"), "multi reset from spent"))
		);

	@Override
	public String[] getValues() {
		return new String[]{Constants.DECIMAL_FORMAT_98.format(moneySpent) + "$"};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"0$"};
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = Constants.PAYMENT_SEND_PATTERN.matcher(event.message.getFormattedText());
		if (matcher.matches())
			setBalance(moneySpent.add(new BigDecimal(matcher.group("amount").replace(",", ""))), "msg: " + IChatComponent.Serializer.componentToJson(event.message));
	}

	@EventListener(triggerWhenDisabled = true)
	public void onTick(TickEvent.ClientTickEvent tickEvent) {
		if (nextReset != -1 && System.currentTimeMillis() > nextReset ) {
			nextReset = getNextServerRestart();
			Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".next_reset", new JsonPrimitive(nextReset));
			setBalance(ZERO, "reset");
			Config.save();
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void loadBalance(GrieferGamesJoinEvent ignored) {
		String path = "modules.money.balances." + mc.getSession().getProfile().getId() + ".";

		if (Config.has(path + "spent") && !resetAfterRestart.get())
			setBalance(BigDecimal.valueOf(Config.get(path + "spent").getAsLong()), "loaded from config: " + path + ": " + Config.get(path + "spent").toString());
		if (Config.has(path + "next_reset")) {
			nextReset = Config.get(path + "next_reset").getAsLong();
			resetSetting.set(nextReset != -1);
		}
	}

	protected static BigDecimal setBalance(BigDecimal newValue, String log) {
		// Temporary, used to debug why the money modules are hallucinating
		System.out.printf("Spent value changed from %f to %f : (%s) %n", moneySpent.doubleValue(), newValue.doubleValue(), log);

		moneySpent = newValue;
		// Save balance along with player uuid so no problems occur when using multiple accounts
		if (!resetAfterRestart.get()) {
			Config.set("modules.money.balances." + mc.getSession().getProfile().getId() + ".spent", new JsonPrimitive(moneySpent));
			Config.save();
		}
		return newValue;
	}
}