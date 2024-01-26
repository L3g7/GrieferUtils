/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.transactions;

public class Transaction implements Comparable<Transaction> {

	public int id;
	public String username;
	public String userId;
	public String recipientname;
	public String recipientId;
	public double amount;
	public long timestamp;

	@Override
	public String toString() {
		return "{" +
			"id=" + id +
			", username='" + username + '\'' +
			", userId='" + userId + '\'' +
			", recipientname='" + recipientname + '\'' +
			", recipientId='" + recipientId + '\'' +
			", amount=" + amount +
			", timestamp=" + timestamp +
			'}';
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Transaction && id == ((Transaction) obj).id;
	}

	@Override
	public int compareTo(Transaction o) {
		return Long.compare(o.timestamp, timestamp);
	}
}