package dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.transactions;

import dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.Packet;

import java.util.List;

public class TransactionsPacket extends Packet {

	public List<Transaction> transactions;

	public TransactionsPacket(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public TransactionsPacket() {}

}