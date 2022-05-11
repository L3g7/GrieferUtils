package dev.l3g7.griefer_utils.event.events.network;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import net.minecraft.network.Packet;

public class PacketReceiveEvent extends Event {

    private final Packet<?> packet;

    public PacketReceiveEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }
}
