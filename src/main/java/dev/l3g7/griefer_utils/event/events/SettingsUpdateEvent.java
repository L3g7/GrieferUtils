package dev.l3g7.griefer_utils.event.events;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import net.labymod.settings.elements.SettingsElement;

import java.util.List;

public class SettingsUpdateEvent extends Event {

	private final List<SettingsElement> settings; // The actual settings
    private final List<SettingsElement> tempElements; // The elements that are being shown

    public SettingsUpdateEvent(List<SettingsElement> settings, List<SettingsElement> tempElements) {
		this.settings = settings;
        this.tempElements = tempElements;
    }

	public List<SettingsElement> getSettings() {
		return settings;
	}

	public List<SettingsElement> getTempElements() {
        return tempElements;
    }

}
