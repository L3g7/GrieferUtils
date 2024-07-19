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

package dev.l3g7.griefer_utils.features.modules.laby3.money;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.item.recraft.laby3.SmallButtonSetting;
import dev.l3g7.griefer_utils.features.modules.Laby3Module;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.util.ResourceLocation;

import java.math.BigDecimal;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.getNextServerRestart;
import static java.math.BigDecimal.ZERO;

@Singleton
@ExclusiveTo(LABY_3)
public class Received extends Laby3Module {

	static BigDecimal moneyReceived = ZERO;
	private static boolean initialized = false; // NOTE cleanup
	private long nextReset = -1;

	private final SwitchSetting resetSetting = SwitchSetting.create()
		.name("Automatisch zurücksetzen")
		.description("Ob automatisch um 04:00 das eingenommene Geld zurückgesetzt werden soll.")
		.icon(ModTextures.SETTINGS_DEFAULT_USE_DEFAULT_SETTINGS)
		.config("modules.money_received.automatically_reset")
		.callback(b -> {
			if (!initialized)
				return;

			if (!b)
				nextReset = -1;
			else
				nextReset = getNextServerRestart();
			Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".next_reset", new JsonPrimitive(nextReset));
			Config.save();
		});

	private static final SwitchSetting resetAfterRestart = SwitchSetting.create()
		.name("Nach Neustart zurücksetzen")
		.description("Ob nach einem Minecraft-Neustart das eingenommene Geld zurückgesetzt werden soll.")
		.icon(ModTextures.SETTINGS_DEFAULT_USE_DEFAULT_SETTINGS)
		.config("modules.money_received.reset_after_restart")
		.callback(shouldReset -> {
			if (!initialized)
				return;

			if (shouldReset)
				Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".received", new JsonPrimitive(ZERO));
			else
				Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".received", new JsonPrimitive(moneyReceived));
			Config.save();
		});

	public static SmallButtonSetting getResetButton() {
		SmallButtonSetting sbs = new SmallButtonSetting(new IconData(new ResourceLocation("griefer_utils", "icons/arrow_circle.png")));
		sbs.setDisplayName("Zurücksetzen");
		sbs.setDescriptionText("Setzt das eingenommene Geld zurück.");
		sbs.buttonIcon(new IconData(ModTextures.BUTTON_TRASH));
		sbs.buttonCallback(() -> setBalance(ZERO));
		return sbs;
	}

	public static SmallButtonSetting getResetAllButton() {
		SmallButtonSetting sbs = new SmallButtonSetting(new IconData(new ResourceLocation("griefer_utils", "icons/arrow_circle.png")));
		sbs.setDisplayName("Alles zurücksetzen");
		sbs.setDescriptionText("Setzt das eingenommene und das ausgegebene Geld zurück.");
		sbs.buttonIcon(new IconData(ModTextures.BUTTON_TRASH));
		sbs.buttonCallback(() -> setBalance(Spent.setBalance(ZERO)));
		return sbs;
	}

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Eingenommen")
		.description("Zeigt dir, wie viel Geld du seit deinem Minecraft-Start eingenommen hast.")
		.icon("wallets/ingoing")
		.subSettings(resetSetting, resetAfterRestart);

	private Received() {
		((SettingsElement) enabled).getSubSettings().add(getResetButton());
		((SettingsElement) enabled).getSubSettings().add(getResetAllButton());
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
	public void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = Constants.PAYMENT_RECEIVE_PATTERN.matcher(event.message.getFormattedText());
		if (matcher.matches())
			setBalance(moneyReceived.add(new BigDecimal(matcher.group("amount").replace(",", ""))));
	}

	@EventListener(triggerWhenDisabled = true)
	public void onTick(TickEvent.ClientTickEvent tickEvent) {
		if (nextReset != -1 && System.currentTimeMillis() > nextReset ) {
			nextReset = getNextServerRestart();
			Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".next_reset", new JsonPrimitive(nextReset));
			setBalance(ZERO);
			Config.save();
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void loadBalance(GrieferGamesJoinEvent ignored) {
		String path = "modules.money.data." + mc.getSession().getProfile().getId() + ".";

		if (Config.has(path + "received") && !resetAfterRestart.get())
			setBalance(BigDecimal.valueOf(Config.get(path + "received").getAsLong()));
		if (Config.has(path + "next_reset")) {
			nextReset = Config.get(path + "next_reset").getAsLong();
			resetSetting.set(nextReset != -1);
		}

		initialized = true;
	}

	protected static BigDecimal setBalance(BigDecimal newValue) {
		moneyReceived = newValue;
		if (!resetAfterRestart.get()) {
			Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".received", new JsonPrimitive(moneyReceived));
			Config.save();
		}
		return newValue;
	}

}