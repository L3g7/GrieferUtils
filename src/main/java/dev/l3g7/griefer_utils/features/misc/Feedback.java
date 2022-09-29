package dev.l3g7.griefer_utils.features.misc;

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
import java.nio.file.Files;
import java.util.Base64;
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

		StringSetting title = new StringSetting()
			.name("Titel")
			.defaultValue("");

		BookSetting description = (BookSetting) new BookSetting()
			.name("Beschreibung");

		FileSetting attachments = (FileSetting) new FileSetting("Feedback - " + name + " - Dateianhang")
			.name("Dateianhang §8(optional)");

		StringSetting contact = new StringSetting()
			.name("Kontaktmöglichkeit")
			.description("Wie können wir dich bei möglichen Rückfragen erreichen?");


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
						title.set("");
						description.clearPages();
						attachments.getFiles();
						contact.set("");
						displayAchievement("Danke \u2764", "Vielen Dank für dein Feedback!");
					}
				})
		);
	}

	public static boolean sendFeedback(ApiEndpoint endpoint, String title, String description, List<File> files, String contact) {

		JsonObject fileData = new JsonObject();

		try {
			for (File file : files) {
				if (!file.exists())
					throw new IOException(file.getAbsolutePath() + " does not exist");

				byte[] bytes = Files.readAllBytes(file.toPath());
				fileData.addProperty(file.getName(), new String(Base64.getEncoder().encode(bytes)));
			}
		} catch (IOException e) {
			e.printStackTrace();
			displayAchievement("Fehler", "Es ist Fehler beim Hochladen der Dateien aufgetreten.");
			return false;
		}

		JsonObject data = new JsonObject();

		data.addProperty("title", title);
		data.addProperty("description", description);
		data.add("files", fileData);
		data.addProperty("contact", contact);
		data.addProperty("sender", name() + " | " + uuid().toString());

		IOUtil.request("https://grieferutils.l3g7.dev/feedback/" + endpoint.url)
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
