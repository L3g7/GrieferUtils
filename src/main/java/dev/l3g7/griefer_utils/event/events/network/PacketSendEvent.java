package dev.l3g7.griefer_utils.event.events.network;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import net.minecraft.network.Packet;

public class PacketSendEvent extends Event.Cancelable {

	private final Packet<?> packet;

	public PacketSendEvent(Packet<?> packet) {
		this.packet = packet;
	}

	public Packet<?> getPacket() {
		return packet;
	}
}
