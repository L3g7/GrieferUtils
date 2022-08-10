package dev.l3g7.griefer_utils.settings;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.Transactions;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.SettingsElement;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class MainPage {

	public static final BooleanSetting features = new BooleanSetting()
			.name("Features")
			.icon("cpu")
			.defaultValue(true)
			.config("features.active")
			.subSettings(
					new HeaderSetting("§r"),
					new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
					new HeaderSetting("§e§lFeatures").scale(.7),
					new HeaderSetting("§r").scale(.4).entryHeight(10));

	public static final BooleanSetting tweaks = new BooleanSetting()
			.name("Tweaks")
			.icon("wrench_screwdriver")
			.defaultValue(true)
			.config("tweaks.active")
			.subSettings(
					new HeaderSetting("§r"),
					new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
					new HeaderSetting("§e§lTweaks").scale(.7),
					new HeaderSetting("§r").scale(.4).entryHeight(10));

	public static final List<SettingsElement> settings = new ArrayList<>(Arrays.asList(
			new HeaderSetting("§r"),
			new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
			new HeaderSetting("§e§lStartseite").scale(.7),
			new HeaderSetting("§r").scale(.4).entryHeight(10),
			features, tweaks,
			Reflection.loadClass(Transactions.class).getMainElement(),
			new HeaderSetting()));

	public MainPage() {
		features.subSettings(loadSubSettings(Feature.Category.FEATURE));
		tweaks.subSettings(loadSubSettings(Feature.Category.TWEAK));
		settings.addAll(loadSubSettings(Feature.Category.MISC));
	}

	private List<SettingsElement> loadSubSettings(Feature.Category category) {
		return FileProvider.getAllClasses()
				.filter(c -> Reflection.hasSuperclass(c, Feature.class))
				.filter(c -> !Modifier.isAbstract(c.getModifiers()))
				.map(Reflection::loadClass)
				.map(Feature.class::cast)
				.filter(f -> f.getCategory() == category)
				.sorted()
				.map(Feature::getMainElement)
				.collect(Collectors.toList());
	}
}
