package dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.auth;

import dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.Packet;

import java.util.UUID;

public class LoginStartPacket extends Packet {

	public UUID uuid;

	public LoginStartPacket() {}

	public LoginStartPacket(UUID uuid) {
		this.uuid = uuid;
	}

}