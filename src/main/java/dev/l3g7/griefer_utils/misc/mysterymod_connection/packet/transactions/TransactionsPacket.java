package dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.transactions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.Packet;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.util.BufUtil;
import io.netty.buffer.ByteBuf;
import net.labymod.utils.JsonParse;

import java.util.ArrayList;
import java.util.List;

public class TransactionsPacket extends Packet {

	private final List<Transaction> transactions = new ArrayList<>();

    @Override
    public void read(ByteBuf buf) {
    	for(JsonElement e : JsonParse.parse(new String(BufUtil.readByteArray(buf))).getAsJsonArray()) {
    		JsonObject o = e.getAsJsonObject();
			transactions.add(new Transaction(o.get("id").getAsInt(), o.get("username").getAsString(), o.get("recipientname").getAsString(), o.get("amount").getAsString(), o.get("timestamp").getAsLong()));
    	}
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

}
