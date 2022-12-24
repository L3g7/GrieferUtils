package dev.l3g7.griefer_utils.event.events.chat;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.misc.TickScheduler;

import java.util.ArrayList;
import java.util.List;

public class ChatLineAddEvent extends Event {

	private static final List<String> lines = new ArrayList<>();

	private final String message;

	public ChatLineAddEvent(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public static void onLineAdd(String message, boolean refresh) {
		if (refresh)
			return;

		// I know it's a scuffed way of doing it, but injecting where LabyMod's filters are triggered causes some wierd crashes with the transformer :|
		// Will be cleaned later anyway
		if (lines.isEmpty()) {
			TickScheduler.runNextRenderTick(() -> {
				String msg = lines.stream().reduce(String::concat).orElseThrow(() -> new RuntimeException("wtf"));
				msg = msg.replaceAll("§.", "");
				EventBus.post(new ChatLineAddEvent(msg));

				lines.clear();
			});
		}

		lines.add(message);
	}

}
