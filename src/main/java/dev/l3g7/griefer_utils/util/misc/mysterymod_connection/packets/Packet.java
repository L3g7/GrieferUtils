package dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets;

import java.util.UUID;

public abstract class Packet {

	public boolean hasUuid = true;
	public UUID uuid;

	public Packet() {}

	public Packet(boolean hasUuid) {
		this();
		this.hasUuid = hasUuid;
	}

}
