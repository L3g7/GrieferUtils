package dev.l3g7.griefer_utils.settings;

import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.Transactions;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.SettingsElement;

import java.lang.reflect.Modifier;
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

    public static List<SettingsElement> settings = Arrays.asList(
            new HeaderSetting("§r"),
            new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
            new HeaderSetting("§e§lStartseite").scale(.7),
            new HeaderSetting("§r").scale(.4).entryHeight(10),
            features, tweaks, Transactions.element);

    public MainPage() {
        populateSubsettings(features, Feature.Category.FEATURE);
        populateSubsettings(tweaks, Feature.Category.TWEAK);
    }

    private void populateSubsettings(BooleanSetting setting, Feature.Category category) {
        setting.subSettings(
                FileProvider.getAllClasses()
                        .filter(c -> Reflection.hasSuperclass(c, Feature.class))
                        .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                        .map(Reflection::loadClass)
                        .map(Feature.class::cast)
                        .filter(f -> f.getCategory() == category)
                        .sorted()
                        .map(Feature::getMainElement)
                        .collect(Collectors.toList())
        );
    }
}
