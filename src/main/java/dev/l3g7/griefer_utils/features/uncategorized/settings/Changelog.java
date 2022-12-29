package dev.l3g7.griefer_utils.features.uncategorized.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.chat.chat_reactor.CategorySetting;
import dev.l3g7.griefer_utils.features.uncategorized.settings.auto_update.ChangelogScreen;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.TextSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.misc.VersionComparator;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Changelog {

	public static final CategorySetting category = new CategorySetting()
		.name("§eChangelog")
		.description("§eVerbindet...")
		.icon("white_scroll")
		.settingsEnabled(false)
		.subSettings();

	static {
		IOUtil.read("https://api.github.com/repos/L3g7/GrieferUtils/releases").asJsonArray(releases -> {
			List<SettingsElement> entries = new ArrayList<>();
			for (JsonElement releaseElement : releases) {
				JsonObject release = releaseElement.getAsJsonObject();

				if (!ChangelogScreen.hasData()) {
					ChangelogScreen.setData(
						release.get("tag_name").getAsString(),
						release.get("body").getAsString().substring("Changelog:\r".length())
					);
				}

				String title = "§l" + release.get("tag_name").getAsString();

				entries.add(new CategorySetting()
					.name(" " + title)
					.subSettings(Arrays.asList(
						new HeaderSetting("§r"),
						new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
						new HeaderSetting("§e§lChangelog - " + title).scale(.7),
						new TextSetting(release.get("body").getAsString().replace("\r", ""))
					)));
			}

			entries.sort(Comparator.comparing(SettingsElement::getDisplayName, new VersionComparator()));
			category.subSettings(entries);

			category.name("§y§fChangelog")
				.description()
				.settingsEnabled(true);
		}).orElse(() ->
			category.name("§y§c§mChangelog")
				.description("§cEs gab einen Fehler.")
				.settingsEnabled(false)
		);
	}
}