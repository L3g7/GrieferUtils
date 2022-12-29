package dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.transactions;

import dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.Packet;
import dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.Protocol.FixedWidth;

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