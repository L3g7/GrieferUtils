package dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.auth;

import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.Packet;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.util.BufUtil;
import io.netty.buffer.ByteBuf;

public class AuthKeysPacket extends Packet {

    private String serverId;
    private byte[] sharedSecret;
    private byte[] verifyToken;

    @Override
    public void read(ByteBuf buf) {
        serverId = BufUtil.readString(buf);
        sharedSecret = BufUtil.readByteArray(buf);
        verifyToken = BufUtil.readByteArray(buf);
    }

    public String getServerId() {
        return serverId;
    }

    public byte[] getSharedSecret() {
        return sharedSecret;
    }

    public byte[] getVerifyToken() {
        return verifyToken;
    }
}
