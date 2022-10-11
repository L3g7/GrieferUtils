package dev.l3g7.griefer_utils.features.misc;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.features.chat_menu.ChatMenuEntry;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;

@Singleton
public class Discord extends Feature {

	private final SmallButtonSetting button = new SmallButtonSetting()
		.name("§z§fDiscord")
		.description("Der Knopf, um dem GrieferUtils-Discord beizutreten.")
		.icon("discord")
		.buttonIcon(new ControlElement.IconData("griefer_utils/icons/open.png"))
		.callback(() -> ChatMenuEntry.openWebsite("https://grieferutils.l3g7.dev"));

	@Override
	public SettingsElement getMainElement() {
		return button;
	}

	public Discord() {
		super(Category.MISC);
	}

}
