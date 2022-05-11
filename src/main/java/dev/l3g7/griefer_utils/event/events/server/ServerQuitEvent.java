package dev.l3g7.griefer_utils.event.events.server;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import net.labymod.utils.ServerData;

public class ServerQuitEvent extends Event {

    private final ServerData data;

    public ServerQuitEvent(ServerData data) {
        this.data = data;
    }

    public ServerData getData() {
        return data;
    }

}