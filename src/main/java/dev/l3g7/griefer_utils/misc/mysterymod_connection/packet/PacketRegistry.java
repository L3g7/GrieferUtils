package dev.l3g7.griefer_utils.misc.mysterymod_connection.packet;

import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.auth.*;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.keepalive.KeepAliveACKPacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.keepalive.KeepAlivePacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.transactions.RequestTransactionsPacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.transactions.TransactionsPacket;

import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {

    public static Map<Byte, Class<? extends Packet>> registry = new HashMap<>();

    static {
        registry.put((byte) 10, RequestAuthPacket.class);
        registry.put((byte) 11, StartAuthPacket.class);
        registry.put((byte) 12, AuthKeysPacket.class);
        registry.put((byte) 13, AuthPacket.class);
        registry.put((byte) 14, CheckAuthPacket.class);
        registry.put((byte) -57, RequestTransactionsPacket.class);
        registry.put((byte) -58, TransactionsPacket.class);
        registry.put((byte) 46, KeepAlivePacket.class);
        registry.put((byte) 47, KeepAliveACKPacket.class);
    }

}
