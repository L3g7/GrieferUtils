/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.transactions;

import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.Packet;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.Protocol.FixedWidth;

import java.util.UUID;

public class RequestTransactionsPacket extends Packet {

    public UUID player;
	@FixedWidth
	public final int serverType = 3;

	public RequestTransactionsPacket() { }

	public RequestTransactionsPacket(UUID player) {
		this.player = player;
	}

}