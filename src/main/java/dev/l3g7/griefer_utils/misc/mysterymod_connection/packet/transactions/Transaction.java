package dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.transactions;

import java.io.Serializable;

public class Transaction implements Serializable {

    private static final long serialVersionUID = 5025303084172648170L;

    private final int id;
    private final String senderName;
    private final String receiverName;
    private final String amount;
    private final long timestamp;

    public Transaction(int id, String senderName, String receiverName, String amount, long timestamp) {
        this.id = id;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
