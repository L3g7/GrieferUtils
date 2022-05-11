package dev.l3g7.griefer_utils.misc.mysterymod_connection.event;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.auth.CheckAuthPacket;

public class StateChangeEvent extends Event {

    private final CheckAuthPacket.State state;

    public StateChangeEvent(CheckAuthPacket.State state) {
        this.state = state;
    }

    public CheckAuthPacket.State getState() {
        return state;
    }

}
