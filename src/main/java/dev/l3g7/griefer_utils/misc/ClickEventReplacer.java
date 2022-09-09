package dev.l3g7.griefer_utils.misc;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.event_bus.EventPriority;
import dev.l3g7.griefer_utils.event.events.chat.MessageModifyEvent;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.features.chat_menu.ChatMenu;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.misc.Constants.*;

/**
 * Replaces the click events in chat to fix crashes when right-clicking a bedrock player and to hide the popup if chat menu is active.
 */
public class ClickEventReplacer {

	public static final String COMMAND = "/grieferutils_click_event_replace_suggest_msg ";
	private static final Pattern[] MESSAGE_PATTERNS = new Pattern[] {PLOTCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN};

	@EventListener(priority = EventPriority.HIGHEST)
	public static void modifyMessage(MessageModifyEvent event) {

		boolean isChatMenuEnabled = FileProvider.getSingleton(ChatMenu.class).isActive();

		// Replaces all /msg click-events
		event.setMessage(replaceClickEvents(event.getMessage(), isChatMenuEnabled));

		if (!isChatMenuEnabled)
			return;

		String name = null;

		for (Pattern p : MESSAGE_PATTERNS) {
			Matcher m = p.matcher(event.getMessage().getFormattedText());

			if (m.find()) {
				name = m.group("name").replaceAll("§.", "");
				break;
			}
		}

		if (name == null)
			return;

		if (name.startsWith("~"))
			name = NameCache.getName(name);

		event.setMessage(setEvent(name, event.getMessage()));
	}

	@EventListener
	public static void onMessageSend(MessageSendEvent event) {
		if (event.getMsg().startsWith(COMMAND)) {
			Feature.suggest("/msg " + event.getMsg().substring(COMMAND.length()));
			event.setCanceled(true);
		}
	}

	private static IChatComponent setEvent(String name, IChatComponent msg) {
		msg.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMMAND + name));
		return msg;
	}

	private static IChatComponent replaceClickEvents(IChatComponent component, boolean isChatMenuActive) {
		for (IChatComponent msg : component.getSiblings()) {
			ChatStyle style = msg.getChatStyle();
			ClickEvent event;

			if (style != null && (event = style.getChatClickEvent()) != null) {
				String value = event.getValue();

				if (value.startsWith("/msg ") && (value.startsWith("/msg !") || isChatMenuActive))
					setEvent(value.substring(5), msg);
			}
		}

		return component;
	}
}
