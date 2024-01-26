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

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.transactions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.StringSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.network.MysteryModConnectionEvent.MMPacketReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.MysteryModConnectionEvent.MMStateChangeEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.misc.TickScheduler;
import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.MysteryModConnection;
import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.MysteryModConnection.State;
import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.transactions.RequestTransactionsPacket;
import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.transactions.Transaction;
import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.transactions.TransactionsPacket;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.minecraft.init.Items;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.uuid;

@Singleton
public class Transactions extends Feature {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private static final List<BaseSetting<?>> HEADER = Arrays.asList(
		HeaderSetting.create("§r"),
		HeaderSetting.create("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
		HeaderSetting.create("§f§lTransaktionen").scale(.7).entryHeight(7),
		HeaderSetting.create("§fder letzten 30 Tage").scale(.7).entryHeight(10),
		HeaderSetting.create("§c§nDie Beträge sind abgerundet§c!").scale(.7)
	);

	private final Set<Transaction> transactions = Collections.synchronizedSet(new TreeSet<>());
	private final Gson PRETTY_PRINTING_GSON = new GsonBuilder().setPrettyPrinting().create();

	@MainElement
	private final CategorySetting setting = CategorySetting.create()
		.name("Transaktionen")
		.description("§eVerbindet...")
		.icon("scroll")
		.disable()
		.subSettings(HEADER);

	@EventListener
	public void onMMPacket(MMPacketReceiveEvent<TransactionsPacket> event) {
		transactions.addAll(event.packet.transactions);
		updateSettings();
	}

	@EventListener
	public void onMMStateChange(MMStateChangeEvent event) {
		State state = event.state;
		if (state == State.CONNECTED) {
			setting.name("Transaktionen")
				.description("Zeigt die Transaktionen der letzen 30 Tage an.")
				.enable();

			// Send Transaction packet every 3s
			MysteryModConnection.eventLoopGroup.scheduleAtFixedRate(() -> event.ctx.writeAndFlush(new RequestTransactionsPacket(uuid())), 0, 3, TimeUnit.SECONDS);
		} else {
			setting.name("§c§mTransaktionen")
				.description("§cMysteryMod ist nicht erreichbar:", state.errorMessage, "", "Joine auf einen Server, um die Verbindung erneut zu versuchen!");
		}
	}

	private void updateSettings() {
		List<BaseSetting<?>> list = setting.getSubSettings();

		list.clear();
		list.addAll(HEADER);

		// Add transactions count
		list.add(HeaderSetting.create("Insgesamt " + (transactions.size() == 1 ? "eine Transaktion" : transactions.size() + " Transaktionen")));
		list.add(HeaderSetting.create("§r").scale(.4).entryHeight(10));

		// Add export setting
		DropDownSetting<ExportFormat> export = DropDownSetting.create(ExportFormat.class) // TODO:, 1)
			.name("Transaktionen exportierten")
			.defaultValue(ExportFormat.NO_SELECTION)
			.icon(Items.writable_book);

		export.callback(format -> {
			if (format == ExportFormat.NO_SELECTION)
				return;

			try {
				export(format);
			} catch (IOException e) {
				LabyBridge.labyBridge.notifyError("Datei konnte nicht erstellt werden.");
				e.printStackTrace();
			}

			export.set(ExportFormat.NO_SELECTION);
		});

		list.add(export);

		// Add filter
		StringSetting filter = StringSetting.create()
			.name("Suche")
			.icon("magnifying_glass")
			.callback(this::updateFilter);

		list.add(filter);
		list.add(HeaderSetting.create("§r").entryHeight(10));

		// Add transactions
		List<Transaction> transactions = new ArrayList<>(this.transactions);
		for (Transaction t : transactions) {
			if (t.recipientname == null)
				continue;

			Direction direction = Direction.get(t);

			String amountStr = Constants.DECIMAL_FORMAT_98.format(t.amount);
			String title = "§l" + amountStr + "$§";

			List<BaseSetting<?>> subSettings = new ArrayList<>(Arrays.asList(
				HeaderSetting.create("§r"),
				HeaderSetting.create("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
				HeaderSetting.create("§f§lTransaktion #" + t.id).scale(.7)
			));

			// Add sender/receiver by direction
			switch (direction) {
				case SENT:
					title = "§c" + title + "c an §l" + t.recipientname;
					subSettings.add(HeaderSetting.create("§lEmpfänger: §r" + t.recipientname).entryHeight(11));
					break;
				case RECEIVED:
					title = "§a" + title + "a von §l" + t.username;
					subSettings.add(HeaderSetting.create("§lSender: §r" + t.username).entryHeight(11));
					break;
				case SELF:
					title = "§e" + title + "e an dich";
					break;
			}

			// Add amount and timestamp
			subSettings.add(HeaderSetting.create("§lBetrag: §r" + amountStr + "$").entryHeight(11));
			subSettings.add(HeaderSetting.create("§lZeitpunkt: §r" + DATE_FORMAT.format(new Date(t.timestamp))).entryHeight(11));

			list.add(CategorySetting.create().name(" " + title).subSettings(subSettings));
		}

		// Update
		TickScheduler.runAfterRenderTicks(() -> {
/*TODO:			if (!(mc().currentScreen instanceof LabyModAddonsGui))
				return;

			if (path().size() == 0 || path().get(path().size() - 1) != setting)
				return;*/

			updateFilter();
		}, 1);
	}

	private void updateFilter() {
		TickScheduler.runAfterRenderTicks(() -> {
/*TODO:			if (!(mc().currentScreen instanceof LabyModAddonsGui))
				return;*/

			List<BaseSetting<?>> listedElementsStored = new ArrayList<>(Reflection.get(mc().currentScreen, "listedElementsStored"));
			listedElementsStored.removeIf(setting -> setting instanceof CategorySetting);

			StringSetting filterSetting = listedElementsStored.stream()
				.filter(s -> s instanceof StringSetting)
				.map(s -> (StringSetting) s)
				.findFirst().orElse(null);

			String filter = filterSetting == null ? "" : filterSetting.get();
			boolean dotMode = filter.contains(".");

			getMainElement().getSubSettings().stream()
				.filter(setting -> {
					if (!(setting instanceof CategorySetting))
						return false;

					String text = setting.name().toLowerCase()
						.replaceAll("§.", "");

					if (!dotMode)
						text = text.replace(".", "");

					return text.contains(filter.toLowerCase());
				})
				.forEach(listedElementsStored::add);
			Reflection.set(mc().currentScreen, "listedElementsStored", listedElementsStored);
		}, 1);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void export(ExportFormat format) throws IOException {
		File file = new File("GrieferUtils", "Transaktionen." + format.fileSuffix);
		file.getParentFile().mkdirs();
		if (file.exists())
			file.delete();
		file.createNewFile();

		List<Transaction> transactions = new ArrayList<>(this.transactions);

		try (OutputStream stream = Files.newOutputStream(file.toPath());
		     OutputStreamWriter writer = new OutputStreamWriter(stream)) {
			switch (format) {
				case TEXT:
					for (Transaction t : transactions) {
						String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(t.timestamp));
						String typeString = t.username.equals(MinecraftUtil.name()) ? t.username.equals(t.recipientname) ? "an dich selbst gezahlt" : "an " + t.recipientname + " gezahlt" : "von " + t.username + " bekommen";
						String amount = String.valueOf(t.amount).replaceFirst("\\.0*$|(\\.\\d*?)0+$", "$1");
						writer.write(String.format("[%s] $%s %s%n", date, amount, typeString));
					}
					break;

				case CSV:
					writer.write("Transaktionsid;Sender;Empfänger;Sender-Id;Empfänger-Id;Betrag;Zeitpunkt\n");
					for (Transaction t : transactions)
						writer.write(String.format("%s;%s;%s;%s;%s;%s;%s%n", t.id, t.username, t.recipientname, t.userId, t.recipientId, t.amount, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(t.timestamp))));
					break;

				case JSON:
					JsonArray beautifiedTransactions = new JsonArray();
					for (Transaction t : transactions) {
						JsonObject object = new JsonObject();
						object.addProperty("transaction_id", t.id);
						object.addProperty("sender_name", t.username);
						object.addProperty("receiver_name", t.recipientname);
						object.addProperty("sender_uuid", t.userId);
						object.addProperty("receiver_uuid", t.recipientId);
						object.addProperty("amount", t.amount);
						object.addProperty("unix_timestamp", t.timestamp);
						beautifiedTransactions.add(object);
					}

					try (JsonWriter jsonWriter = new JsonWriter(writer)) {
						jsonWriter.setIndent("\t");
						PRETTY_PRINTING_GSON.toJson(beautifiedTransactions, jsonWriter);
					}
					break;

				case PPTX:
					new TransactionPPTXWriter(transactions, stream).write();
					break;
			}
		}

		Desktop.getDesktop().open(file);
	}

	public enum Direction {

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

	enum ExportFormat implements Named {

		NO_SELECTION("§7-", null),
		TEXT("Text", "txt"),
		JSON("JSON", "json"),
		CSV("CSV", "csv"),
		PPTX("PPTX", "pptx");

		final String name;
		final String fileSuffix;

		ExportFormat(String name, String fileSuffix) {
			this.name = name;
			this.fileSuffix = fileSuffix;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}