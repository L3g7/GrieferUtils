package dev.l3g7.griefer_utils.laby3.settings;

import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.laby3.settings.types.SwitchSettingImpl;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;

public class MainPage {

	public static List<BaseSetting<?>> collectSettings() {
		List<BaseSetting<?>> settings = new ArrayList<>(Arrays.asList(
			HeaderSetting.create("§r"),
			HeaderSetting.create("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
			HeaderSetting.create("§e§lStartseite").scale(.7),
			HeaderSetting.create("§r").scale(.4).entryHeight(10),
// TODO		filter,
			HeaderSetting.create("§r").scale(.4).entryHeight(10)));

		// Enable the feature category if one of its features gets enabled
		Feature.getFeatures()
			.sorted(Comparator.comparing(f -> f.getMainElement().name()))
			.forEach(feature -> {
				if (!feature.getClass().isAnnotationPresent(Feature.FeatureCategory.class)
					|| !(feature.getMainElement() instanceof SwitchSettingImpl main))
					return;

				for (BaseSetting<?> element : main.getChildSettings()) {
					if (!(element instanceof SwitchSetting sub))
						continue;

					sub.callback(b -> {
						if (b)
							main.set(true);
					});
				}
			});

		// Add features to categories
		Feature.getFeatures()
			.sorted(Comparator.comparing(f -> f.getMainElement().name()))
			.forEach(Feature::addToCategory);

		// Add categories
		Feature.getCategories().stream()
			.sorted(Comparator.comparing(BaseSetting::name))
			.forEach(settings::add);

		settings.add(HeaderSetting.create());

		// Add uncategorized features
		Feature.getUncategorized().stream()
			.sorted(Comparator.comparing(BaseSetting::name))
			.forEach(settings::add);

		settings.add(HeaderSetting.create());

		// Wiki link
		settings.add(ButtonSetting.create()
			.name("Wiki").icon("open_book")
			.buttonIcon("open_book_outline")
			.callback(() -> labyBridge.openWebsite("https://grieferutils.l3g7.dev/wiki")));

		// Ko-fi link
		settings.add(ButtonSetting.create()
			.name("Entwickler unterstützen").icon("ko_fi")
			.description("Wenn dir das Addon gefällt kannst du hier das Entwickler-Team dahinter unterstützen §c❤")
			.buttonIcon("ko_fi_outline")
			.callback(() -> labyBridge.openWebsite("https://ko-fi.com/l3g7_3")));

		// Discord link
		settings.add(ButtonSetting.create()
			.name("Discord").icon("discord")
			.buttonIcon("discord_clyde")
			.callback(() -> labyBridge.openWebsite("https://grieferutils.l3g7.dev/discord")));

		// Create settings
		for (BaseSetting<?> setting : settings)
			setting.create(null);

		return settings;
	}

}
