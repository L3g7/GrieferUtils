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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
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
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static dev.l3g7.griefer_utils.features.uncategorized.Transactions.ExportFormat.NO_SELECTION;
import static dev.l3g7.griefer_utils.features.uncategorized.Transactions.ExportFormat.PPTX;
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

		// Add export setting
		DropDownSetting<ExportFormat> export = new DropDownSetting<>(ExportFormat.class, 1)
			.name("Transaktionen exportierten")
			.defaultValue(NO_SELECTION)
			.icon(Material.BOOK_AND_QUILL);

		export.callback(format -> {
			if (format == NO_SELECTION)
				return;

			try {
				export(format);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			export.set(NO_SELECTION);
		});

		list.add(export);

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

			String filter = ((StringSetting) listedElementsStored.get(8)).get();
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

	// TODO beautify this mess
	// maybe remove in next update?

	private final Gson PRETTY_PRINTING_GSON = new GsonBuilder().setPrettyPrinting().create();

	private void export(ExportFormat format) throws IOException {
		File file = new File("GrieferUtils", "Transaktionen." + format.fileSuffix);
		file.getParentFile().mkdirs();
		if (file.exists())
			file.delete();
		file.createNewFile();

		List<Transaction> transactions = new ArrayList<>(this.transactions);

		if (format == PPTX)
			try (OutputStream out = Files.newOutputStream(file.toPath())) {
				exportPptx(out);
			}
		else {
			try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()))) {
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
				}
			}
		}

		Desktop.getDesktop().open(file);
	}

	// TODO beautify this mess
	// boilerplate, hardcoded, ineffective, ugly
	private void exportPptx(OutputStream fileOut) throws IOException {
		int transactionCount = transactions.size();
		ZipOutputStream out = new ZipOutputStream(fileOut);

		// Prepare strings for injection
		StringBuilder injectContentTypes = new StringBuilder();
		for (int i = 0; i < transactionCount; i++)
			injectContentTypes.append("<Override PartName=\"/ppt/slides/slide").append(i + 3).append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slide+xml\"/>");
		StringBuilder injectPresentationSldIdLst = new StringBuilder();
		for (int i = 0; i < transactionCount; i++)
			injectPresentationSldIdLst.append("<p:sldId id=\"").append(i + 1000).append("\" r:id=\"rId").append(i + 13).append("\"/>");
		StringBuilder injectPresentationRels = new StringBuilder();
		for (int i = 0; i < transactionCount; i++)
			injectPresentationRels.append("<Relationship Id=\"rId").append(i + 13).append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide\" Target=\"slides/slide").append(i + 3).append(".xml\"/>");
		double received = transactions.stream().filter(t->Direction.get(t)==Direction.RECEIVED).mapToDouble(t->t.amount).sum();
		double spent = transactions.stream().filter(t->Direction.get(t)==Direction.SENT).mapToDouble(t->t.amount).sum();
		String receivedStr = format(received);
		String spentStr = format(spent);
		String earned = format(received-spent);

		int maxLen = Math.max(receivedStr.length(), Math.max(spentStr.length(), earned.length()));

		// Create zip (copy from template and inject)
		ZipInputStream in = new ZipInputStream(FileProvider.getData("assets/minecraft/griefer_utils/transactions_export_template"));
		ZipEntry entry;
		while ((entry = in.getNextEntry()) != null) {
			out.putNextEntry(new ZipEntry(entry.getName()));
			byte[] buffer = new byte[4096];
			int bytesRead;

			// Inject
			if (entry.getName().endsWith("[Content_Types].xml") ||
				entry.getName().endsWith("presentation.xml") ||
				entry.getName().endsWith("presentation.xml.rels") ||
				entry.getName().endsWith("slide1.xml") ||
				entry.getName().endsWith("slide2.xml")) {
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				while ((bytesRead = in.read(buffer, 0, 4096)) != -1)
					b.write(buffer, 0, bytesRead);
				String s = b.toString();
				s = s.replace("{InjectContentTypes}", injectContentTypes)
					.replace("{InjectPresentationSldIdLst}", injectPresentationSldIdLst)
					.replace("{InjectPresentationRels}", injectPresentationRels)
					.replace("{InjectFrontSlideName}", MinecraftUtil.name())
					.replace("{InjectSummarySlideReceived}", leftPad(receivedStr, maxLen) + "$")
					.replace("{InjectSummarySlideSpent}", " " + leftPad(spentStr, maxLen) + "$")
					.replace("{InjectSummarySlideEarned}", "   " + leftPad(earned, maxLen) + "$");
				out.write(s.getBytes());
			} else
				while ((bytesRead = in.read(buffer, 0, 4096)) != -1)
					out.write(buffer, 0, bytesRead);
		}

		// Create slides
		int i = 0;
		for (Transaction transaction : transactions) {
			out.putNextEntry(new ZipEntry("ppt/slides/slide" + (i + 3) + ".xml"));
			out.write((String.format("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><p:sld xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\"><p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id=\"1\" name=\"\"/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/><a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"0\" cy=\"0\"/></a:xfrm></p:grpSpPr><p:sp><p:nvSpPr><p:cNvPr id=\"2\" name=\"Titel 1\"><a:extLst><a:ext uri=\"{FF2B5EF4-FFF2-40B4-BE49-F238E27FC236}\"><a16:creationId xmlns:a16=\"http://schemas.microsoft.com/office/drawing/2014/main\" id=\"{75010B25-D580-DF75-8D9E-34616B09724D}\"/></a:ext></a:extLst></p:cNvPr><p:cNvSpPr><a:spLocks noGrp=\"1\"/></p:cNvSpPr><p:nvPr><p:ph type=\"title\"/></p:nvPr></p:nvSpPr><p:spPr/><p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:rPr lang=\"de-DE\" dirty=\"0\"/><a:t>Transaktion #%d</a:t></a:r></a:p></p:txBody></p:sp><mc:AlternateContent xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\"><mc:Choice xmlns:a14=\"http://schemas.microsoft.com/office/drawing/2010/main\" Requires=\"a14\"><p:sp><p:nvSpPr><p:cNvPr id=\"3\" name=\"Inhaltsplatzhalter 2\"><a:extLst><a:ext uri=\"{FF2B5EF4-FFF2-40B4-BE49-F238E27FC236}\"><a16:creationId xmlns:a16=\"http://schemas.microsoft.com/office/drawing/2014/main\" id=\"{36686E2E-EC35-D61C-06BA-609DC6AF0E37}\"/></a:ext></a:extLst></p:cNvPr><p:cNvSpPr><a:spLocks noGrp=\"1\"/></p:cNvSpPr><p:nvPr><p:ph idx=\"1\"/></p:nvPr></p:nvSpPr><p:spPr/><p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:pPr marL=\"0\" indent=\"0\"><a:buNone/></a:pPr><a:r><a:rPr lang=\"de-DE\" dirty=\"0\"/><a:t>%s </a:t></a:r><a14:m><m:oMath xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\"><m:r><a:rPr lang=\"de-DE\" i=\"1\" dirty=\"0\" smtClean=\"0\"><a:latin typeface=\"Cambria Math\" panose=\"02040503050406030204\" pitchFamily=\"18\" charset=\"0\"/></a:rPr><m:t>→</m:t></m:r></m:oMath></a14:m><a:r><a:rPr lang=\"de-DE\" dirty=\"0\"/><a:t> %s</a:t></a:r></a:p><a:p><a:pPr marL=\"0\" indent=\"0\"><a:buNone/></a:pPr><a:r><a:rPr lang=\"de-DE\" dirty=\"0\"/><a:t>Betrag: %s$</a:t></a:r></a:p><a:p><a:pPr marL=\"0\" indent=\"0\"><a:buNone/></a:pPr><a:r><a:rPr lang=\"de-DE\" dirty=\"0\"/><a:t>Zeitpunkt: </a:t></a:r><a:r><a:rPr lang=\"de-DE\"/><a:t>%s</a:t></a:r><a:endParaRPr lang=\"de-DE\" dirty=\"0\"/></a:p></p:txBody></p:sp></mc:Choice><mc:Fallback><p:sp><p:nvSpPr><p:cNvPr id=\"3\" name=\"Inhaltsplatzhalter 2\"><a:extLst><a:ext uri=\"{FF2B5EF4-FFF2-40B4-BE49-F238E27FC236}\"><a16:creationId xmlns:a16=\"http://schemas.microsoft.com/office/drawing/2014/main\" id=\"{36686E2E-EC35-D61C-06BA-609DC6AF0E37}\"/></a:ext></a:extLst></p:cNvPr><p:cNvSpPr><a:spLocks noGrp=\"1\" noRot=\"1\" noChangeAspect=\"1\" noMove=\"1\" noResize=\"1\" noEditPoints=\"1\" noAdjustHandles=\"1\" noChangeArrowheads=\"1\" noChangeShapeType=\"1\" noTextEdit=\"1\"/></p:cNvSpPr><p:nvPr><p:ph idx=\"1\"/></p:nvPr></p:nvSpPr><p:spPr><a:blipFill><a:blip r:embed=\"rId3\"/><a:stretch><a:fillRect l=\"-1217\" t=\"-2241\"/></a:stretch></a:blipFill></p:spPr><p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:rPr lang=\"de-DE\"><a:noFill/></a:rPr><a:t> </a:t></a:r></a:p></p:txBody></p:sp></mc:Fallback></mc:AlternateContent></p:spTree><p:extLst><p:ext uri=\"{BB962C8B-B14F-4D97-AF65-F5344CB8AC3E}\"><p14:creationId xmlns:p14=\"http://schemas.microsoft.com/office/powerpoint/2010/main\" val=\"1776503031\"/></p:ext></p:extLst></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr><mc:AlternateContent xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\"><mc:Choice xmlns:p14=\"http://schemas.microsoft.com/office/powerpoint/2010/main\" Requires=\"p14\"><p:transition p14:dur=\"250\"><p:fade/></p:transition></mc:Choice><mc:Fallback><p:transition><p:fade/></p:transition></mc:Fallback></mc:AlternateContent></p:sld>", transaction.id, transaction.username, transaction.recipientname, transaction.amount, new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(transaction.timestamp)))).getBytes());
			out.putNextEntry(new ZipEntry("ppt/slides/_rels/slide" + (i + 3) + ".xml.rels"));
			out.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"../media/image5.png\"/><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout\" Target=\"../slideLayouts/slideLayout2.xml\"/></Relationships>").getBytes());
			i++;
		}
		out.closeEntry();
		out.close();

	}

	private String format(double value) {
		String s = new BigDecimal(String.valueOf(value)).toPlainString();
		if (s.length() == s.indexOf(".") + 2)
			s += "0";
		return s.replace('.', ',');
	}
	private String leftPad(String str, int targetLen) {
		StringBuilder strBuilder = new StringBuilder(str);
		while (strBuilder.length() < targetLen)
			strBuilder.insert(0, " ");
		return strBuilder.toString();
	}

	enum ExportFormat {

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

	}

}