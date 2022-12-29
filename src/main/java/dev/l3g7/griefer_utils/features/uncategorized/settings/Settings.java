package dev.l3g7.griefer_utils.features.uncategorized.settings;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.chat.chat_reactor.CategorySetting;
import dev.l3g7.griefer_utils.features.uncategorized.settings.auto_update.AutoUpdate;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;

@Singleton
public class Settings extends Feature {

	@MainElement(configureSubSettings = false)
	private final CategorySetting element = new CategorySetting()
		.name("§yEinstellungen")
		.icon("cog")
		.subSettings(AutoUpdate.enabled, Changelog.category, new HeaderSetting(), Telemetry.category);

}