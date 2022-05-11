package dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.auth;

import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.Packet;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.util.BufUtil;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class StartAuthPacket extends Packet {

    private final UUID uuid;

    @Override
    public void write(ByteBuf buf) {
        BufUtil.writeUUID(buf, uuid);
    }


    public StartAuthPacket(UUID uuid) {
        this.uuid = uuid;
    }

}
