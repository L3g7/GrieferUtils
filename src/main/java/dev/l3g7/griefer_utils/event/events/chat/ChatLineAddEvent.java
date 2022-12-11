package dev.l3g7.griefer_utils.event.events.chat;

import dev.l3g7.griefer_utils.event.event_bus.Event;

public class ChatLineAddEvent extends Event {

	private final String message;

	public ChatLineAddEvent(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
