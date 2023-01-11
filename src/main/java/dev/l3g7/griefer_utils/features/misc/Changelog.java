package dev.l3g7.griefer_utils.features.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.misc.update_screen.UpdateScreen;
import dev.l3g7.griefer_utils.features.misc.update_screen.V2InfoScreen;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.VersionComparator;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.TextSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Singleton
public class Changelog extends Feature {

	private final CategorySetting element = new CategorySetting()
			.name("§y§eChangelog")
			.description("§eVerbindet...")
			.icon("white_scroll")
			.settingsEnabled(false)
			.subSettingsWithHeader("Changelog");

	public Changelog() {
		super(Category.MISC);

		IOUtil.request("https://api.github.com/repos/L3g7/GrieferUtils/releases").asJsonArray(releases -> {
			List<SettingsElement> entries = new ArrayList<>();
			for (JsonElement releaseElement : releases) {
				JsonObject release = releaseElement.getAsJsonObject();

				if (!UpdateScreen.hasData()) {
					UpdateScreen.setData(
						release.get("tag_name").getAsString(),
						release.get("body").getAsString().substring("Changelog:\r".length())
					);
				}

				String title = "§l" + release.get("tag_name").getAsString();

				entries.add(new CategorySetting()
					.name(" " + title)
					.subSettings(
						new HeaderSetting("§r"),
						new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
						new HeaderSetting("§f§lChangelog - " + title).scale(.7),
						new TextSetting(release.get("body").getAsString().replace("\r", ""))
					));
			}

			entries.sort(Comparator.comparing(SettingsElement::getDisplayName, new VersionComparator()));

			entries.add(0, new HeaderSetting(""));
			entries.add(0, new V2InfoScreen.V2ChangelogSetting());

			element.subSettings(entries);

			element .name("§y§fChangelog")
					.description(null)
					.settingsEnabled(true);
		}).orElse(t ->
			element .name("§y§c§mChangelog")
					.description("§cDu wurdest geratelimited.")
					.settingsEnabled(false)
		);

	}

	@Override
	public SettingsElement getMainElement() {
		return element;
	}
}
