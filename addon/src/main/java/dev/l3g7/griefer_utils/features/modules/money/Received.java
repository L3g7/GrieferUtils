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
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.util.IChatComponent;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;

import static java.math.BigDecimal.ZERO;

@Singleton
public class Received extends Module {

	private static final int HOUR = 60 * 60 * 1000; // An hour, in milliseconds.

	static BigDecimal moneyReceived = ZERO;
	private long nextReset = -1;

	private final BooleanSetting resetSetting = new BooleanSetting()
		.name("Automatisch zurücksetzen")
		.description("Ob automatisch um 04:00 das eingenommene Geld zurückgesetzt werden soll.")
		.icon(ModTextures.SETTINGS_DEFAULT_USE_DEFAULT_SETTINGS)
		.callback(b -> {
			if (!b)
				nextReset = -1;
			else
				nextReset = getNextReset();
			Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".next_reset", new JsonPrimitive(nextReset));
			Config.save();
		});

	public Received() {
		super("Eingenommen", "Zeigt dir, wie viel Geld du seit Minecraft-Start eingenommen hast", "received", new IconData("griefer_utils/icons/wallet_ingoing.png"));
	}


	@Override
	public void fillSubSettings(List<SettingsElement> list) {
		super.fillSubSettings(list);
		list.add(resetSetting);
		list.add(new SmallButtonSetting()
			.name("Zurücksetzen")
			.icon("arrow_circle")
			.buttonIcon(new IconData(ModTextures.BUTTON_TRASH))
			.callback(() -> setBalance(ZERO, "single reset")));

		list.add(new SmallButtonSetting()
			.name("Alles zurücksetzen")
			.icon("arrow_circle")
			.buttonIcon(new IconData(ModTextures.BUTTON_TRASH))
			.callback(() -> setBalance(Spent.setBalance(ZERO, "multi reset from received"), "multi reset from received")));
	}

	@Override
	public String[] getValues() {
		return new String[]{Constants.DECIMAL_FORMAT_98.format(moneyReceived) + "$"};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"0$"};
	}

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = Constants.PAYMENT_RECEIVE_PATTERN.matcher(event.message.getFormattedText());
		if (matcher.matches())
			setBalance(moneyReceived.add(new BigDecimal(matcher.group("amount").replace(",", ""))), "msg: " + IChatComponent.Serializer.componentToJson(event.message));
	}

	private long getNextReset() {
		long time = System.currentTimeMillis();
		long reset = time - time % (24 * HOUR) + (2 * HOUR); // Get timestamp for 02:00 UTC on the current day

		if (System.currentTimeMillis() > reset)
			reset += 24 * HOUR; // When it's already after 02:00 UTC, the next reset is 24h later

		return reset;
	}

	@EventListener
	public void onTick(TickEvent.ClientTickEvent tickEvent) {
		if (nextReset != -1 && System.currentTimeMillis() > nextReset ) {
			nextReset = getNextReset();
			Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".next_reset", new JsonPrimitive(nextReset));
			setBalance(ZERO, "reset");
			Config.save();
		}
	}

	@EventListener
	public void loadBalance(GrieferGamesJoinEvent ignored) {
		String path = "modules.money.data." + mc.getSession().getProfile().getId() + ".";

		if (Config.has(path + "received"))
			setBalance(BigDecimal.valueOf(Config.get(path + "received").getAsLong()), "loaded from config: " + path + ": " + Config.get(path + "received").toString());
		if (Config.has(path + "next_reset")) {
			nextReset = Config.get(path + "next_reset").getAsLong();
			resetSetting.set(nextReset != -1);
		}
	}

	protected static BigDecimal setBalance(BigDecimal newValue, String log) {
		// Temporary, used to debug why the money modules are hallucinating
		System.out.printf("Received value changed from %f to %f, stored as %s: %s%n", moneyReceived.doubleValue(), newValue.doubleValue(), new JsonPrimitive(newValue), log);

		moneyReceived = newValue;
		Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".received", new JsonPrimitive(moneyReceived));
		Config.save();
		return newValue;
	}

}