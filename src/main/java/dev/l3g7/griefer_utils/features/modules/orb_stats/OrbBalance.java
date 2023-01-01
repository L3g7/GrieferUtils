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

package dev.l3g7.griefer_utils.features.modules.orb_stats;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.ServerJoinEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.misc.Config;
import dev.l3g7.griefer_utils.util.misc.ServerCheck;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class OrbBalance extends Module {

	private static final Pattern SKULL_PATTERN = Pattern.compile("^§7Du besitzt aktuell §e(?<orbs>[\\d.]+) Orbs§7\\.$");
	private static final Pattern BUY_PATTERN = Pattern.compile("^\\[GrieferGames] Du hast erfolgreich das Produkt .+ für (?<orbs>[\\d.]+) Orbs gekauft\\.$");
	public static final Pattern ORB_SELL_PATTERN = Pattern.compile("^\\[Orbs] Du hast erfolgreich (?<amount>[\\d.]+) (?<item>[\\S ]+) für (?<orbs>[\\d.]+) Orbs verkauft\\.$");
	public static final DecimalFormat DECIMAL_FORMAT_3 = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.GERMAN));

	private static long balance = -1;

	public OrbBalance() {
		super("Orbguthaben", "Zeigt dir an, wie viele Orbs du hast.", "orb_balance", new ControlElement.IconData("griefer_utils/icons/orb.png"));
	}

	@EventListener
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!ServerCheck.isOnCitybuild() || !(mc.currentScreen instanceof GuiChest))
			return;

		IInventory inv = Reflection.get(mc.currentScreen, "lowerChestInventory");

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
			|| !ItemUtil.getLore(skull).isEmpty())
			return;

		Matcher matcher = SKULL_PATTERN.matcher(ItemUtil.getLastLore(skull));
		if (matcher.matches()) {
			balance = Long.parseLong(matcher.group("orbs").replace(".", ""));
			saveBalance();
		}
	}

	@EventListener
	public void onMessageReceive(ClientChatReceivedEvent event) {
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

	@EventListener
	public void loadBalance(ServerJoinEvent ignored) {
		if (!ServerCheck.isOnGrieferGames())
			return;

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