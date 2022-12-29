package dev.l3g7.griefer_utils.features.uncategorized;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import net.labymod.settings.elements.ControlElement;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Singleton
public class Discord extends Feature {

	@MainElement
	private final SmallButtonSetting button = new SmallButtonSetting()
		.name("§zDiscord")
		.icon("discord")
		.buttonIcon(new ControlElement.IconData("griefer_utils/icons/discord_clyde.png"))
		.callback(() -> {
			try {
				Desktop.getDesktop().browse(new URI("https://grieferutils.l3g7.dev/discord"));
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException(e);
			}
		});

}