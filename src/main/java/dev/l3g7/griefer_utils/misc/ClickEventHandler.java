package dev.l3g7.griefer_utils.misc;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.event_bus.EventPriority;
import dev.l3g7.griefer_utils.event.events.chat.MessageModifyEvent;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.features.chat_menu.ChatMenu;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import net.labymod.utils.ModColor;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.misc.Constants.*;

/**
 * Replaces the click events in chat to hide the popup if chat menu is active,
 * adds click events to private- and plot chat messages
 * and makes /tpaccept and /tpdeny clickable.
 */
public class ClickEventHandler {

	public static final String COMMAND = "/grieferutils_click_event_replace_suggest_msg ";

	private static final String TP_ACCEPT = "Um die Anfrage anzunehmen, schreibe /tpaccept.";
	private static final String TP_DENY = "Um sie abzulehnen, schreibe /tpdeny.";
	private static final List<Pattern> MESSAGE_PATTERNS = ImmutableList.of(PLOTCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN);
	private static final Map<String, String> GLOBALCHAT_CB_TO_SWITCH = new HashMap<String, String>(){{
		put("CBE", "cbevil");
		put("WASSER", "farm1");
		put("LAVA", "nether1");
		put("EVENT", "eventserver");
	}};

	@EventListener(priority = EventPriority.HIGHEST)
	public static void modifyMessage(MessageModifyEvent event) {
		modifyGlobalChats(event);
		modifyStatuses(event);
		modifyMsgs(event);
		modifyTps(event);
	}

	private static void modifyGlobalChats(MessageModifyEvent event) {
		String unformattedText = event.getMessage().getUnformattedText();
		if (!unformattedText.startsWith("@["))
			return;

		String cb = unformattedText.substring(2, unformattedText.indexOf(']'));

		IChatComponent message = event.getMessage();

		for (IChatComponent sibling : message.getSiblings()) {
			if (!sibling.getUnformattedTextForChat().equals(cb))
				continue;

			sibling.getChatStyle()
				.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/switch " + GLOBALCHAT_CB_TO_SWITCH.getOrDefault(cb, cb)))
				.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§6Klicke, um auf den CB zu wechseln")));
			break;
		}

		event.setMessage(message);
	}

	private static void modifyStatuses(MessageModifyEvent event) {
		String formattedText = event.getMessage().getFormattedText();
		Matcher matcher = STATUS_PATTERN.matcher(formattedText);
		if (!matcher.matches())
			return;

		String name = matcher.group("name");
		IChatComponent message = event.getMessage();

		for (IChatComponent part : message.getSiblings())
			part.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/msg %s ", ModColor.removeColor(name))));

		event.setMessage(message);
	}

	private static void modifyMsgs(MessageModifyEvent event) {
		if (!FileProvider.getSingleton(ChatMenu.class).isActive())
			return;

		// Replaces all /msg click-events
		event.setMessage(replaceMsgClickEvents(event.getMessage()));

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

		event.setMessage(setClickEvent(event.getMessage(), COMMAND + name));
	}

	private static void modifyTps(MessageModifyEvent event) {
		String msg = event.getMessage().getUnformattedText();

		if (!msg.equals(TP_ACCEPT) && !msg.equals(TP_DENY))
			return;

		String command = msg.equals(TP_ACCEPT) ? "/tpaccept" : "/tpdeny";

		IChatComponent component = event.getMessage();

		for (IChatComponent part : component.getSiblings())
			if (part.getUnformattedText().equals(command))
				part.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));

		event.setMessage(component);
	}

	@EventListener
	public static void onMessageSend(MessageSendEvent event) {
		if (event.getMsg().startsWith(COMMAND)) {
			Feature.suggest("/msg " + event.getMsg().substring(COMMAND.length()));
			event.setCanceled(true);
		}
	}

	private static IChatComponent replaceMsgClickEvents(IChatComponent component) {
		for (IChatComponent msg : component.getSiblings()) {
			ChatStyle style = msg.getChatStyle();
			ClickEvent event;

			if (style != null && (event = style.getChatClickEvent()) != null) {
				String value = event.getValue();

				if (value.startsWith("/msg "))
					setClickEvent(msg, COMMAND + value.substring(5));
			}
		}

		return component;
	}

	private static IChatComponent setClickEvent(IChatComponent msg, String command) {
		msg.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		return msg;
	}

}
