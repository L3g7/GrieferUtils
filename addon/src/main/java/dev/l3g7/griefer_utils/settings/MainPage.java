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

package dev.l3g7.griefer_utils.settings;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.events.GuiInitEvent;
import dev.l3g7.griefer_utils.features.Category;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.FeatureCategory;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.misc.badges.GrieferUtilsGroup;
import dev.l3g7.griefer_utils.settings.elements.*;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.crash.CrashReport;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

/**
 * The main page of the addon's settings.
 */
public class MainPage {

	private static Timer timer = new Timer();

	private static final StringSetting filter = new StringSetting()
		.name("Suche")
		.icon("magnifying_glass")
		.callback(MainPage::onSearch);

	public static final List<SettingsElement> settings = new ArrayList<>(Arrays.asList(
		new HeaderSetting("§r"),
		new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
		new HeaderSetting("§e§lStartseite").scale(.7),
		new HeaderSetting("§r").scale(.4).entryHeight(10),
		filter,
		new HeaderSetting("§r").scale(.4).entryHeight(10)));

	private static final List<SettingsElement> searchableSettings = new ArrayList<>();

	static {
		try {
			loadFeatures();
		} catch (Throwable t) {
			mc().displayCrashReport(new CrashReport("GrieferUtils konnte nicht geladen werden!", t));
		}
	}

	private static void loadFeatures() {
		// Load features
		List<Feature> features = new ArrayList<>();
		FileProvider.getClassesWithSuperClass(Feature.class).forEach(meta -> {
			if (!meta.isAbstract())
				features.add(FileProvider.getSingleton(meta.load()));
		});

		features.sort(Comparator.comparing(f -> f.getMainElement().getDisplayName()));
		for (Feature feature : features) {
			feature.getCategory().add(feature);

			if (feature.getClass().isAnnotationPresent(FeatureCategory.class)) {
				feature.getMainElement().getSubSettings().getElements().stream()
					.filter(e -> e instanceof BooleanSetting || e instanceof NumberSetting || e instanceof CategorySetting)
					.forEachOrdered(searchableSettings::add);

				// Enable the feature category when one of its features is enabled
				if (feature.getMainElement() instanceof BooleanSetting) {
					for (SettingsElement element : feature.getMainElement().getSubSettings().getElements()) {
						if (element instanceof BooleanSetting) {
							((BooleanSetting) element).callback(b -> {
								if (b) {
									((BooleanSetting) feature.getMainElement()).set(true);
								}
							});
						}
					}
				}
			} else {
				searchableSettings.add(feature.getMainElement());
			}
		}

		for (net.labymod.ingamegui.Module module : Module.getModules())
			if (module.getCategory() == Module.CATEGORY && module instanceof Module)
				searchableSettings.add(new ModuleProxySetting((Module) module));

		searchableSettings.sort(Comparator.comparing(SettingsElement::getDisplayName));

		// Add every category to the main page
		Category.getCategories().stream()
			.filter(c -> c.getConfigKey() != null)
			.map(Category::getSetting)
			.sorted(Comparator.comparing(SettingsElement::getDisplayName))
			.forEach(settings::add);

		settings.add(new HeaderSetting());

		Category.getCategories().stream()
			.filter(c -> c.getConfigKey() == null)
			.flatMap(s -> s.getSetting().getSubSettings().getElements().stream())
			.sorted(Comparator.comparing(SettingsElement::getDisplayName))
			.forEach(settings::add);

		settings.add(new HeaderSetting());

		// Wiki link
		settings.add(new SmallButtonSetting()
			.name("Wiki").icon("open_book")
			.buttonIcon(new ControlElement.IconData("griefer_utils/icons/open_book_outline.png"))
			.callback(() -> Util.openWebsite("https://grieferutils.l3g7.dev/wiki")));

		// Ko-fi link
		settings.add(new SmallButtonSetting()
			.name("Entwickler unterstützen").icon("ko_fi")
			.description("Wenn dir das Addon gefällt kannst du hier das Entwickler-Team dahinter unterstützen §c❤")
			.buttonIcon(new ControlElement.IconData("griefer_utils/icons/ko_fi_outline.png"))
			.callback(() -> Util.openWebsite("https://ko-fi.com/l3g7_3")));

		// Discord link
		settings.add(new SmallButtonSetting()
			.name("Discord").icon("discord")
			.buttonIcon(new ControlElement.IconData("griefer_utils/icons/discord_clyde.png"))
			.callback(() -> Util.openWebsite("https://grieferutils.l3g7.dev/discord")));
	}

	private static void onSearch() {
		TickScheduler.runAfterRenderTicks(() -> {
			if (!(mc().currentScreen instanceof LabyModAddonsGui))
				return;

			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = digest.digest(("griefer_utils_salt_" + filter.get()).getBytes(StandardCharsets.UTF_8));
			String hash = Base64.getEncoder().encodeToString(bytes);

			if (hash.equals("IBmzqW3cyeMT0Gj/VqDLhnOhI0Qdhx6FgqFsdLbvzGA=")) {
				timer = new Timer();
				timer.schedule(new TimerTask() {
					public void run() {
						if (!(mc().currentScreen instanceof LabyModAddonsGui))
							return;

						GrieferUtilsGroup.icon = GrieferUtilsGroup.icon.equals("icon") ? filter.get() : "icon";
						filter.set("");
						displayAchievement("§aEaster Egg", "Easter Egg wurde umgeschalten.");
						if (world() != null)
							mc().displayGuiScreen(null);

						timer = null;
					}
				}, 3179);
			} else if (timer != null) {
				timer.cancel();
				timer = null;
			}

			List<SettingsElement> listedElementsStored = Reflection.get(mc().currentScreen, "tempElementsStored");

			if (filter.get().isEmpty()) {
				listedElementsStored.clear();
				listedElementsStored.addAll(settings);
				return;
			}

			int startIndex = 6;
			while (listedElementsStored.size() > startIndex)
				listedElementsStored.remove(startIndex);

			searchableSettings.stream()
				.filter(s -> s.getDisplayName().replaceAll("§.", "").toLowerCase().contains(filter.get().toLowerCase()))
				.forEach(listedElementsStored::add);
		}, 1);
	}

	@EventListener
	private static void onGuiInit(GuiInitEvent event) {
		if (!(event.screen instanceof LabyModAddonsGui))
			return;

		filter.getStorage().value = "";

		Reflection.set(filter, "", "currentValue");
		ModTextField textField = Reflection.get(filter, "textField");
		textField.setText("");
		textField.setFocused(false);

		filter.unfocus(0, 0, 0);
	}

}
