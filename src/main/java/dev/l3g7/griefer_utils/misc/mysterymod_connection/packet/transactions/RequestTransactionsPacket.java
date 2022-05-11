package dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.transactions;

import java.util.UUID;

import dev.l3g7.griefer_utils.misc.mysterymod_connection.util.BufUtil;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.Packet;
import io.netty.buffer.ByteBuf;

public class RequestTransactionsPacket extends Packet {

    private final UUID player;

    public RequestTransactionsPacket(UUID player) {
        this.player = player;
    }

    @Override
    public void write(ByteBuf buf) {
        BufUtil.writeUUID(buf, player);
        buf.writeInt(3); // ServerType?
    }

}
