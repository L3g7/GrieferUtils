package dev.l3g7.griefer_utils.features.chat;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageAboutToBeSentEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.util.ChatAllowedCharacters;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.HIGH;

@Singleton
public class FixKicks extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Kicks verhindern")
		.description("Verhindert Kicks aufgrund ungültiger Zeichen in gesendeten Nachrichten.")
		.icon("chat_checkmark");

	@EventListener(priority = HIGH)
	private void onMessageSend(MessageAboutToBeSentEvent event) {
		if (event.message.chars().anyMatch(c -> !ChatAllowedCharacters.isAllowedCharacter((char) c))) {
			event.cancel();
			display(Constants.ADDON_PREFIX + "§cUngültiges Zeichen in \"" + event.message + "§r§c\"");
		}
	}

}
