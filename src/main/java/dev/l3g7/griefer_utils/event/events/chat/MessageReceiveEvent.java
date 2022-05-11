package dev.l3g7.griefer_utils.event.events.chat;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import net.minecraft.util.IChatComponent;

public class MessageReceiveEvent extends Event.Cancelable {

    private final IChatComponent component;
    private final String formatted, unformatted;

    public MessageReceiveEvent(IChatComponent component) {
        this.component = component;
        formatted = component.getFormattedText();
        unformatted = component.getUnformattedText();
    }

    public String getFormatted() {
        return formatted;
    }

    public String getUnformatted() {
        return unformatted;
    }

    public IChatComponent getComponent() {
        return component;
    }

}