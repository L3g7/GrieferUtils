package dev.l3g7.griefer_utils.misc.mysterymod_connection.event;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.Packet;

public class PacketReceiveEvent extends Event {

    private final Packet packet;

    public PacketReceiveEvent(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }
}
