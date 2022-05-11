package dev.l3g7.griefer_utils.event.events;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import net.labymod.settings.elements.SettingsElement;

import java.util.List;

public class SettingsUpdateEvent extends Event {

    private final List<SettingsElement> list;

    public SettingsUpdateEvent(List<SettingsElement> list) {
        this.list = list;
    }

    public List<SettingsElement> getList() {
        return list;
    }

}
