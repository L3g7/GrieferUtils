package dev.l3g7.griefer_utils.event.events.chat;

import dev.l3g7.griefer_utils.event.event_bus.Event;

public class MessageDisplayEvent extends Event.Cancelable {

    private final String msg;

    public MessageDisplayEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
