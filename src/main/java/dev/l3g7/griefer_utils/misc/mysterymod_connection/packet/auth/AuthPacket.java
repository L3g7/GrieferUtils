package dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.auth;

import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.Packet;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.util.BufUtil;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class AuthPacket extends Packet {

    private final UUID uuid;
    private final String username;
    private final byte[] sharedSecret;
    private final byte[] verifyToken;
    private final String actualVersion;
    private final String modVersion;

    @Override
    public void write(ByteBuf buf) {
        BufUtil.writeUUID(buf, uuid);
        BufUtil.writeString(buf, username);
        BufUtil.writeByteArray(buf, sharedSecret);
        BufUtil.writeByteArray(buf, verifyToken);
        BufUtil.writeString(buf, actualVersion);
        BufUtil.writeString(buf, modVersion);
    }

    public AuthPacket(UUID uuid, String username, byte[] sharedSecret, byte[] verifyToken, String actualVersion, String modVersion) {
        this.uuid = uuid;
        this.username = username;
        this.sharedSecret = sharedSecret;
        this.verifyToken = verifyToken;
        this.actualVersion = actualVersion;
        this.modVersion = modVersion;
    }

}
