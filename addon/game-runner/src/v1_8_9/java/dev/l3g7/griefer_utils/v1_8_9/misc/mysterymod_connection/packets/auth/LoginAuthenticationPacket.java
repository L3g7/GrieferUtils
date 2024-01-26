/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.auth;

import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.Packet;

import java.util.UUID;

/**
 * The packet handling authentication of the client.
 * Request (S2C) -> Start (C2S) -> Encryption (S2C) -> Authentication (C2S) -> Response (S2C)
 */
public class LoginAuthenticationPacket extends Packet {

	public UUID uuid;
	public String username;

	/**
	 * A 128-bit AES key, encrypted using the pK gotten in {@link LoginEncryptionPacket}.
	 * It does not seem to be used, as the connection is unencrypted.
	 */
	public byte[] sharedSecret;

	/**
	 * The verify-token gotten in {@link LoginEncryptionPacket}, encrypted using the pK gotten in {@link LoginEncryptionPacket}.
	 */
	public byte[] verifyToken;
	public String actualVersion;
	public String modVersion;

	public LoginAuthenticationPacket() {}

	public LoginAuthenticationPacket(UUID uuid, String username, byte[] sharedSecret, byte[] verifyToken, String actualVersion, String modVersion) {
		this.uuid = uuid;
		this.username = username;
		this.sharedSecret = sharedSecret;
		this.verifyToken = verifyToken;
		this.actualVersion = actualVersion;
		this.modVersion = modVersion;
	}

}