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

package dev.l3g7.griefer_utils.v1_8_9.features.modules.orb_stats;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Module;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class OrbBalance extends Module {

	private static final Pattern SKULL_PATTERN = Pattern.compile("^§7Du besitzt aktuell §e(?<orbs>[\\d.]+) Orbs§7\\.$");
	private static final Pattern BUY_PATTERN = Pattern.compile("^\\[GrieferGames] Du hast erfolgreich das Produkt .+ für (?<orbs>[\\d.]+) Orbs gekauft\\.$");
	public static final Pattern ORB_SELL_PATTERN = Pattern.compile("^\\[Orbs] Du hast erfolgreich (?<amount>[\\d.]+) (?<item>[\\S ]+) für (?<orbs>[\\d.]+) Orbs verkauft\\.$");
	public static final DecimalFormat DECIMAL_FORMAT_3 = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.GERMAN));

	private static long balance = -1;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Orbguthaben")
		.description("Zeigt dir an, wie viele Orbs du hast.")
		.icon("orb");

	@EventListener(triggerWhenDisabled = true)
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!ServerCheck.isOnCitybuild() || !(mc().currentScreen instanceof GuiChest))
			return;

		IInventory inv = Reflection.get(mc().currentScreen, "lowerChestInventory");

		int skullSlot;
		if (inv.getName().equals("§6Händler"))
			skullSlot = 10;
		else if (inv.getName().equals("§6Verkäufer"))
			skullSlot = 13;
		else
			return;

		ItemStack skull = inv.getStackInSlot(skullSlot);
		if (skull == null
			|| !(skull.getItem() instanceof ItemSkull)
			|| !skull.getDisplayName().equals("§6Deine Orbs")
			|| ItemUtil.getLore(skull).isEmpty())
			return;

		Matcher matcher = SKULL_PATTERN.matcher(ItemUtil.getLastLore(skull));
		if (matcher.matches()) {
			balance = Long.parseLong(matcher.group("orbs").replace(".", ""));
			saveBalance();
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(MessageReceiveEvent event) {
		if (!ServerCheck.isOnCitybuild())
			return;

		String msg = event.message.getUnformattedText();

		Matcher sellMatcher = ORB_SELL_PATTERN.matcher(msg);
		if (sellMatcher.matches()) {
			balance += Long.parseLong(sellMatcher.group("orbs").replace(".", ""));
			saveBalance();
			return;
		}

		Matcher buyMatcher = BUY_PATTERN.matcher(msg);
		if (buyMatcher.matches()) {
			balance -= Long.parseLong(buyMatcher.group("orbs").replace(".", ""));
			saveBalance();
		}
	}

	@Override
	public String[] getValues() {
		return balance == -1 ? getDefaultValues() : new String[]{DECIMAL_FORMAT_3.format(balance)};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"Bitte öffne den Orb-Händler / Orb-Verkäufer."};
	}

	@EventListener(triggerWhenDisabled = true)
	public void loadBalance(GrieferGamesJoinEvent ignored) {
		String path = "modules.orb_balance.balances." + mc().getSession().getProfile().getId();

		if (Config.has(path))
			balance = Config.get(path).getAsLong();
	}

	private void saveBalance() {
		// Save balance along with player uuid so no problems occur when using multiple accounts
		Config.set("modules.orb_balance.balances." + mc().getSession().getProfile().getId(), new JsonPrimitive(balance));
		Config.save();
	}

	public static long getBalance() {
		return balance;
	}

}