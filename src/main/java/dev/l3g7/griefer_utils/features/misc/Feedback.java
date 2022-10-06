package dev.l3g7.griefer_utils.features.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.*;
import dev.l3g7.griefer_utils.settings.elements.filesetting.FileSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Singleton
public class Feedback extends Feature {

	private final CategorySetting general = addSettings(
		new CategorySetting()
			.name("Generelles Feedback")
			.icon(Material.BOOK_AND_QUILL),
		ApiEndpoint.GENERAL
	);

	private final CategorySetting bugReport = addSettings(
		new CategorySetting()
			.name("Fehler melden")
			.icon("bug"),
		ApiEndpoint.BUG
	);

	private final CategorySetting suggestion = addSettings(new CategorySetting()
			.name("Feature / Änderung vorschlagen")
			.icon(Material.NETHER_STAR),
		ApiEndpoint.SUGGESTION
	);

	private final CategorySetting category = new CategorySetting()
		.name("§2§fFeedback")
		.icon(Material.BOOK_AND_QUILL)
		.subSettingsWithHeader("Feedback", general, bugReport, suggestion);

	public Feedback() {
		super(Category.MISC);
	}

	@Override
	public SettingsElement getMainElement() {
		return category;
	}

	private CategorySetting addSettings(CategorySetting setting, ApiEndpoint endpoint) {
		String name = setting.getDisplayName();

		StringSetting title = (StringSetting) new StringSetting()
			.name("Titel")
			.defaultValue("")
			.maxLength(256);

		BookSetting description = (BookSetting) new BookSetting()
			.limit(4096)
			.name("Beschreibung");

		FileSetting attachments = (FileSetting) new FileSetting("Feedback - " + name + " - Dateianhang")
			.totalLimit(8_000_000L, "8 MB")
			.name("Dateianhang §8(optional)");

		StringSetting contact = (StringSetting) new StringSetting()
			.name("Kontaktmöglichkeit")
			.description("Wie können wir dich bei möglichen Rückfragen erreichen?")
			.maxLength(1028);


		return setting.subSettingsWithHeader(
			name,
			title,
			description,
			attachments,
			contact,
			new HeaderSetting("")
				.entryHeight(20),
			new ButtonSetting()
				.name("Abschicken")
				.callback(() -> {
					if (sendFeedback(endpoint, title.get(), String.join("\r", description.getPages()), attachments.getFiles(), contact.get())) {
						// Clear all fields
						title.set("");
						description.clearPages();
						attachments.clearFiles();
						contact.set("");
						displayAchievement("Danke \u2764", "Vielen Dank für dein Feedback!");
					}
				})
		);
	}

	public static boolean sendFeedback(ApiEndpoint endpoint, String title, String description, List<File> files, String contact) {

		if (title.isEmpty() || description.isEmpty() || contact.isEmpty()) {
			displayAchievement("§e§l§nFehlende Daten", "§eBitte fülle alle benötigten Felder aus!");
			return false;
		}

		JsonArray fileData = new JsonArray();

		// Add files
		try {
			for (File file : files) {
				if (!file.exists())
					throw new IOException(file.getAbsolutePath() + " does not exist");

				JsonObject fileEntry = new JsonObject();
				fileEntry.addProperty("name", file.getName());
				fileEntry.addProperty("mime_type", URLConnection.guessContentTypeFromName(file.getName()));
				fileEntry.addProperty("data", new String(Files.readAllBytes(file.toPath()), UTF_8));

				fileData.add(fileEntry);
			}
		} catch (IOException e) {
			e.printStackTrace();
			displayAchievement("§c§l§nFehler \u26A0", "§cEs ist Fehler beim Hochladen der Dateien aufgetreten.");
			return false;
		}

		// Add everything to the main payload
		JsonObject data = new JsonObject();

		data.addProperty("title", title);
		data.addProperty("description", description);
		data.add("files", fileData);
		data.addProperty("contact", contact);

		JsonObject sender = new JsonObject();
		sender.addProperty("name", name());
		sender.addProperty("uuid", uuid().toString());

		data.add("sender", sender);

		// Send the payload
		IOUtil.request("https://grieferutils.l3g7.dev/feedback/" + endpoint.url + "/")
			.post("application/json", data.toString().getBytes(UTF_8))
			.close();

		return true;
	}

	public enum ApiEndpoint {
		GENERAL("general_feedback"),
		BUG("bug_report"),
		SUGGESTION("suggestion");

		final String url;
		ApiEndpoint(String url) {
			this.url = url;
		}
	}
}
