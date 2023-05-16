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

package dev.l3g7.griefer_utils.features.uncategorized;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.MysteryModConnectionEvent.MMPacketReceiveEvent;
import dev.l3g7.griefer_utils.event.events.network.MysteryModConnectionEvent.MMStateChangeEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.MysteryModConnection;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.MysteryModConnection.State;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.transactions.RequestTransactionsPacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.transactions.Transaction;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.transactions.TransactionsPacket;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

@Singleton
public class Transactions extends Feature {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private static final List<SettingsElement> HEADER = Arrays.asList(
		new HeaderSetting("§r"),
		new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
		new HeaderSetting("§f§lTransaktionen").scale(.7).entryHeight(7),
		new HeaderSetting("§fder letzten 30 Tage").scale(.7).entryHeight(10),
		new HeaderSetting("§c§nDie Beträge sind abgerundet§c!").scale(.7)
	);

	private List<Transaction> transactions = Collections.emptyList();

	@MainElement
	private final CategorySetting setting = new CategorySetting()
		.name("Transaktionen")
		.description("§eVerbindet...")
		.icon("scroll")
		.settingsEnabled(false)
		.subSettings(HEADER);

	@EventListener
	public void onMMPacket(MMPacketReceiveEvent event) {
		if (event.packet instanceof TransactionsPacket) {
			transactions = ((TransactionsPacket) event.packet).transactions;
			transactions.sort((a, b) -> Integer.compare(b.id, a.id)); // Sort by id
			updateSettings();
		}
	}

	@EventListener
	public void onMMStateChange(MMStateChangeEvent event) {
		State state = event.state;
		if (state == State.CONNECTED) {
			setting.name("Transaktionen")
				.description()
				.settingsEnabled(true);

			// Send Transaction packet every 3s
			MysteryModConnection.eventLoopGroup.scheduleAtFixedRate(() -> event.ctx.writeAndFlush(new RequestTransactionsPacket(uuid())), 0, 3, TimeUnit.SECONDS);
		} else {
			setting.name("§c§mTransaktionen")
				.description("§cMysteryMod ist nicht erreichbar:", state.errorMessage, "", "Joine auf einen Server, um die Verbindung erneut zu versuchen!");
		}
	}

	private void updateSettings() {
		List<SettingsElement> list = setting.getSubSettings().getElements();

		list.clear();
		list.addAll(HEADER);

		// Add transactions count
		list.add(new HeaderSetting("Insgesamt " + (transactions.size() == 1 ? "eine Transaktion" : transactions.size() + " Transaktionen")));
		list.add(new HeaderSetting("§r").scale(.4).entryHeight(10));

		// Add filter
		StringSetting filter = new StringSetting()
			.name("Suche")
			.icon("magnifying_glass")
			.callback(s -> updateFilter());

		list.add(filter);
		list.add(new HeaderSetting("§r").entryHeight(10));

		// Add transactions
		for (Transaction t : transactions) {
			Direction direction = Direction.get(t);

			String amountStr = Constants.DECIMAL_FORMAT_98.format(t.amount);
			String title = "§l" + amountStr + "$§";

			List<SettingsElement> subSettings = new ArrayList<>(Arrays.asList(
				new HeaderSetting("§r"),
				new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
				new HeaderSetting("§f§lTransaktion #" + t.id).scale(.7)
			));

			// Add sender/receiver by direction
			switch (direction) {
				case SENT:
					title = "§c" + title + "c an §l" + t.recipientname;
					subSettings.add(new HeaderSetting("§lEmpfänger: §r" + t.recipientname).entryHeight(11));
					break;
				case RECEIVED:
					title = "§a" + title + "a von §l" + t.username;
					subSettings.add(new HeaderSetting("§lSender: §r" + t.username).entryHeight(11));
					break;
				case SELF:
					title = "§e" + title + "e an dich";
					break;
			}

			// Add amount and timestamp
			subSettings.add(new HeaderSetting("§lBetrag: §r" + amountStr + "$").entryHeight(11));
			subSettings.add(new HeaderSetting("§lZeitpunkt: §r" + DATE_FORMAT.format(new Date(t.timestamp))).entryHeight(11));

			list.add(new CategorySetting().name(" " + title).subSettings(subSettings));
		}

		// Update
		TickScheduler.runAfterRenderTicks(() -> {
			if (!(mc().currentScreen instanceof LabyModAddonsGui))
				return;

			if (path().size() == 0 || path().get(path().size() - 1) != setting)
				return;

			updateFilter();
		}, 1);
	}

	private void updateFilter() {
		TickScheduler.runAfterRenderTicks(() -> {
			if (!(mc().currentScreen instanceof LabyModAddonsGui))
				return;

			List<SettingsElement> listedElementsStored = new ArrayList<>(Reflection.get(mc().currentScreen, "listedElementsStored"));
			listedElementsStored.removeIf(setting -> setting instanceof CategorySetting);

			String filter = ((StringSetting) listedElementsStored.get(7)).get();
			boolean dotMode = filter.contains(".");

			getMainElement().getSubSettings().getElements().stream()
				.filter(setting -> {
					if (!(setting instanceof CategorySetting))
						return false;

					String text = setting.getDisplayName().toLowerCase()
						.replaceAll("§.", "");

					if (!dotMode)
						text = text.replace(".", "");

					return text.contains(filter.toLowerCase());
				})
				.forEach(listedElementsStored::add);
			Reflection.set(mc().currentScreen, listedElementsStored, "listedElementsStored");
		}, 1);
	}

	private enum Direction {

		SENT, RECEIVED, SELF;

		public static Direction get(Transaction t) {
			if (t.recipientname.equals(t.username))
				return SELF;
			if (MinecraftUtil.name().equals(t.username))
				return SENT;
			else
				return RECEIVED;
		}
	}
}