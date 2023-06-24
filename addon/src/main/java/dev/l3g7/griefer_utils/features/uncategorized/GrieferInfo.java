package dev.l3g7.griefer_utils.features.uncategorized;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.DataHandler;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.GuiGrieferInfo;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;

@Singleton
public class GrieferInfo extends Feature {

	private final KeySetting setting = new KeySetting()
		.name("Taste")
		.icon("key")
		.description("Mit welcher Taste das Gui geöffnet werden soll.")
		.pressCallback(b -> { if (b) GuiGrieferInfo.GUI.open(); });

	@MainElement
	private final CategorySetting button = new CategorySetting()
		.name("§xGriefer.Info")
		.icon("griefer_info")
		.subSettings(setting, new HeaderSetting(), new HeaderSetting("Das Griefer.Info Gui lässt sich auch mit /info oder /gi öffnen."));

	@Override
	public void init() {
		super.init();
		DataHandler.requestMetaData();
	}

}
