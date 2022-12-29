package dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.transactions;

public class Transaction {

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
}