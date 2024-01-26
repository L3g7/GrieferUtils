/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.transactions;

import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.Packet;

import java.util.List;

public class TransactionsPacket extends Packet {

	public List<Transaction> transactions;

	public TransactionsPacket(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public TransactionsPacket() {}

}