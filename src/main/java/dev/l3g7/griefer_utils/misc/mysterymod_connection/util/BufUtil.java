package dev.l3g7.griefer_utils.misc.mysterymod_connection.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import io.netty.buffer.ByteBuf;

public class BufUtil { // Too lazy to implement a custom Buffer

	public static UUID readUUID(ByteBuf buf) {
		return new UUID(buf.readLong(), buf.readLong());
	}

	public static void writeUUID(ByteBuf buf, UUID uuid) {
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
	}
	
    public static void writeString(ByteBuf buf, String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    public static String readString(ByteBuf buf) {
        byte[] bytes = new byte[readVarInt(buf)];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeVarInt(ByteBuf buf, int input) {
        while ((input & 0xFFFFFF80) != 0) {
            buf.writeByte(input & 0x7F | 0x80);
            input >>>= 7;
        }
        buf.writeByte(input);
    }

    public static int readVarInt(ByteBuf byteBuf) {
        byte b;
        int i = 0;
        int j = 0;
        do {
            b = byteBuf.readByte();
            i |= (b & 0x7F) << j++ * 7;
            if (j > 5)
                throw new RuntimeException("VarInt is too big");
        } while ((b & 0x80) == 128);
        return i;
    }

    public static void writeByteArray(ByteBuf buf, byte[] bytes) {
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    public static byte[] readByteArray(ByteBuf buf) {
        byte[] bytes = new byte[readVarInt(buf)];
        buf.readBytes(bytes);
        return bytes;
    }

}
