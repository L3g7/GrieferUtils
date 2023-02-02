package dev.l3g7.griefer_utils.event.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ChatLogModifyEvent extends Event {

	public String message;

	public ChatLogModifyEvent(String message) {
		this.message = message;
	}

}
