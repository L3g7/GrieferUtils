package dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.auth;

import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.Packet;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.util.BufUtil;
import io.netty.buffer.ByteBuf;

public class CheckAuthPacket extends Packet {

    private State state;

    @Override
    public void read(ByteBuf buf) {
        state = State.values()[BufUtil.readVarInt(buf)];
    }

    public State getState() {
        return state;
    }

    public enum State {

        SUCCESSFUL, INVALID_SESSION, SESSION_SERVERS_DOWN, ERROR_OCCURRED, LOADING

    }

}
