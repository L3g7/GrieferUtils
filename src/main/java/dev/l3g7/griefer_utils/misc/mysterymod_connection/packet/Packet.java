package dev.l3g7.griefer_utils.misc.mysterymod_connection.packet;

import dev.l3g7.griefer_utils.misc.mysterymod_connection.util.BufUtil;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public abstract class Packet {

    private UUID uuid = null;
    private boolean hasUuid = true;

    public Packet() {}

    public Packet(boolean hasUuid) {
        this.hasUuid = hasUuid;
    }

    public Packet(UUID uuid) {
        this.uuid = uuid;
    }

    public void read(ByteBuf buf) {}
    public void write(ByteBuf buf) {}

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void readUUID(ByteBuf buf) {
        if(hasUuid)
            uuid = BufUtil.readUUID(buf);
    }

    public void writeUUID(ByteBuf buf) {
        if(hasUuid)
            BufUtil.writeUUID(buf, uuid != null ? uuid : UUID.randomUUID());
    }

}