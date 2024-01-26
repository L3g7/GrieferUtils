/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.auth;

import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.Packet;

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