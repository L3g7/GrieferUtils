package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

@Singleton
public class CommandLogger extends Feature {

	private final BooleanSetting enabled = new BooleanSetting()
			.name("CommandLogger")
			.description("Schreibt die Befehle, die du ausführst in die Konsole und damit den Log.")
			.config("tweaks.command_logger.active")
			.icon(Material.COMMAND)
			.defaultValue(true);

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	public CommandLogger() {
		super(Category.TWEAK);
	}

	@EventListener
	public void onMessageSend(MessageSendEvent event) {
		if (isActive() && isOnGrieferGames() && event.getMsg().startsWith("/"))
			System.out.println("Executed command: " + event.getMsg());
	}
}
