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
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Config;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.features.modules.balances.CoinBalance;
import dev.l3g7.griefer_utils.features.player.scoreboard.BankScoreboard;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.getNextServerRestart;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.getServerFromScoreboard;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

@Singleton
public class Received extends Module {

	private static final BigDecimal MINUS_ONE = ZERO.subtract(ONE);

	static BigDecimal moneyReceived = ZERO;
	private BigDecimal previousMoneyOwned = MINUS_ONE;
	private BigDecimal previousDiff = ZERO;

	private final BooleanSetting onlyPayed = new BooleanSetting()
		.name("Nur Zahlungen mit einbeziehen")
		.description("Ob nur Zahlungen von anderen Spielern mit einbezogen werden sollen, oder alle Geldeinnahmen.")
		.icon("coin_pile");

	private final BooleanSetting resetSetting = new BooleanSetting()
		.name("Automatisch zurücksetzen")
		.description("Ob automatisch um 04:00 das eingenommene Geld zurückgesetzt werden soll.")
		.icon(ModTextures.SETTINGS_DEFAULT_USE_DEFAULT_SETTINGS);

	public Received() {
		super("Eingenommen", "Zeigt dir, wie viel Geld du seit Minecraft-Start eingenommen hast", "received", new IconData("griefer_utils/icons/wallet_ingoing.png"));

		long nextReset = Config.has("modules.money.reset") ? Config.get("modules.money.reset").getAsLong() : MinecraftUtil.getNextServerRestart();

		new Timer().schedule(new TimerTask() {
			public void run() {
				if (resetSetting.get())
					TickScheduler.runAfterRenderTicks(() -> setBalance(ZERO), 1);
			}
		}, new Date(nextReset), 24 * 3600 * 1000);
	}


	@Override
	public void fillSubSettings(List<SettingsElement> list) {
		super.fillSubSettings(list);
		list.add(onlyPayed);
		list.add(resetSetting);
		list.add(new SmallButtonSetting()
			.name("Zurücksetzen")
			.icon("arrow_circle")
			.buttonIcon(new IconData(ModTextures.BUTTON_TRASH))
			.callback(() -> setBalance(ZERO)));

		list.add(new SmallButtonSetting()
			.name("Alles zurücksetzen")
			.icon("arrow_circle")
			.buttonIcon(new IconData(ModTextures.BUTTON_TRASH))
			.callback(() -> setBalance(Spent.setBalance(ZERO))));
	}

	@Override
	public String[] getValues() {
		return new String[]{Constants.DECIMAL_FORMAT_98.format(moneyReceived) + "$"};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"0$"};
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMMCustomPayload(MysteryModPayloadEvent event) {
		if (onlyPayed.get() || getServerFromScoreboard().equals("Portal") || getServerFromScoreboard().equals(""))
			return;

		if (event.channel.equals("bank") && previousDiff != null) {
			// Spent money
			if (previousDiff.signum() == -1)
				Spent.setBalance(Spent.moneySpent.add(previousDiff));
				// Received money
			else
				setBalance(moneyReceived.subtract(previousDiff));
			previousDiff = null;
			previousMoneyOwned = getMoneyOwned();
		}

		if (!event.channel.equals("coins"))
			return;

		previousDiff = getMoneyOwned().subtract(previousMoneyOwned);
		previousMoneyOwned = getMoneyOwned();

		// Moved money between the bank and the purse
		if (previousDiff.signum() == 0) {
			previousDiff = null;
			return;
		}

		// Spent money
		if (previousDiff.signum() == -1)
			Spent.setBalance(Spent.moneySpent.subtract(previousDiff));
			// Received money
		else
			setBalance(moneyReceived.add(previousDiff));
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(ClientChatReceivedEvent event) {
		if (!onlyPayed.get())
			return;

		Matcher matcher = Constants.PAYMENT_RECEIVE_PATTERN.matcher(event.message.getFormattedText());
		if (matcher.matches())
			setBalance(moneyReceived.add(new BigDecimal(matcher.group("amount").replace(",", ""))));
	}

	@EventListener
	public void loadBalance(ServerEvent.ServerJoinEvent ignored) {
		if (!ServerCheck.isOnGrieferGames())
			return;

		String key = "modules.money.data." + mc.getSession().getProfile().getId() + ".received";

		if (Config.has(key))
			setBalance(BigDecimal.valueOf(Config.get(key).getAsLong()));
	}

	@EventListener
	public void onCBJoin(CityBuildJoinEvent event) {
		if (previousMoneyOwned.equals(MINUS_ONE))
			previousMoneyOwned = getMoneyOwned();
	}

	protected static BigDecimal setBalance(BigDecimal newValue) {
		moneyReceived = newValue;

		Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".received", new JsonPrimitive(moneyReceived));
		Config.set("modules.money.reset", new JsonPrimitive(getNextServerRestart()));
		Config.save();
		return newValue;
	}

	static BigDecimal getMoneyOwned() {
		return new BigDecimal(BankScoreboard.getBankBalance()).add(BigDecimal.valueOf(CoinBalance.getCoinBalance()));
	}

}