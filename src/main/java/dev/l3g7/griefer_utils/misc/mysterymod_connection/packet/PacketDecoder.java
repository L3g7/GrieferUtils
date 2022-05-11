package dev.l3g7.griefer_utils.misc.mysterymod_connection.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {

    protected void decode(ChannelHandlerContext context, ByteBuf buf, List<Object> out) {
        try {
            int length = buf.readInt();
            if (length > 0) {
                byte packetId = buf.readByte();
                if (!PacketRegistry.registry.containsKey(packetId)) {
                    if (packetId != (byte) -97 && packetId != 99) // idk what these are, can be ignored I guess
                        System.err.println("[MysteryMod] Unknown packet id " + packetId + "!");
                    return;
                }

                Packet packet = PacketRegistry.registry.get(packetId).getConstructor().newInstance();
                packet.readUUID(buf);
                packet.read(buf);
                out.add(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
