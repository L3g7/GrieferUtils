package dev.l3g7.griefer_utils.event.events.chat;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import net.minecraft.util.IChatComponent;

public class MessageModifyEvent extends Event {

    private final IChatComponent original;
    private IChatComponent message;

    public MessageModifyEvent(IChatComponent original) {
        this.original = original;
        message = original.createCopy();
    }

    public IChatComponent getOriginal() {
        return original;
    }

    public IChatComponent getMessage() {
        return message;
    }

    public void setMessage(IChatComponent message) {
        this.message = message;
    }

}
