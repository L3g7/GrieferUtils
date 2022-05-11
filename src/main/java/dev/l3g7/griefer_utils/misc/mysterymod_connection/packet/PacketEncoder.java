package dev.l3g7.griefer_utils.misc.mysterymod_connection.packet;

import java.util.List;
import java.util.Map.Entry;

import java.util.Optional;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class PacketEncoder extends MessageToMessageEncoder<Packet> {

    protected void encode(ChannelHandlerContext context, Packet packet, List<Object> out) {
        try {
            ByteBuf buf = context.alloc().buffer();
            Optional<Entry<Byte, Class<? extends Packet>>> packetId = PacketRegistry.registry.entrySet().stream()
                    .filter(e -> e.getValue() == packet.getClass())
                    .findAny();
            if (!packetId.isPresent())
                throw new Exception("[MysteryMod] packet " + packet.getClass() + " not registered!");

            buf.writeByte(packetId.get().getKey());
            packet.writeUUID(buf);
            packet.write(buf);
            out.add(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
