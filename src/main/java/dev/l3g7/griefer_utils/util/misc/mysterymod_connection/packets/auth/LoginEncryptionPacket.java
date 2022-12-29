package dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.auth;

import dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.Packet;

public class LoginEncryptionPacket extends Packet {

	public String serverId;
	public byte[] sharedSecret;
	public byte[] verifyToken;

	public LoginEncryptionPacket() {}

	public LoginEncryptionPacket(String serverId, byte[] sharedSecret, byte[] verifyToken) {
		this.serverId = serverId;
		this.sharedSecret = sharedSecret;
		this.verifyToken = verifyToken;
	}

}