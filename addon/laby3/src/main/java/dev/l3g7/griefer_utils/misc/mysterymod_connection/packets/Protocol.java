/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.misc.mysterymod_connection.packets;

import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.auth.*;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.keep_alive.KeepAliveACKPacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.keep_alive.KeepAlivePacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.transactions.RequestTransactionsPacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.transactions.TransactionsPacket;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.reflection.Reflection.c;

/**
 * description missing.
 */
public class Protocol {

	public static Map<Byte, Class<? extends Packet>> registry = new ImmutableMap.Builder<Byte, Class<? extends Packet>>()
		.put((byte) 10, LoginRequestPacket.class)
		.put((byte) 11, LoginStartPacket.class)
		.put((byte) 12, LoginEncryptionPacket.class)
		.put((byte) 13, LoginAuthenticationPacket.class)
		.put((byte) 14, LoginResponsePacket.class)
		.put((byte) -57, RequestTransactionsPacket.class)
		.put((byte) -58, TransactionsPacket.class)
		.put((byte) 46, KeepAlivePacket.class)
		.put((byte) 47, KeepAliveACKPacket.class)
		.build();

	public static <T> T read(ByteBuf buf, Class<T> t, boolean fixedWidth) {
		if (t == int.class) {
			if (fixedWidth)
				return c(buf.readInt());

			// encoded in 7 bit chunks, with the 8th bit indicating whether a chunk is following
			byte b = -1;
			int value = 0;
			for (int index = 0; (b & 0x80) == 128; index++) {
				if (index > 5)
					throw new DecoderException("VarInt is too big");
				b = buf.readByte();
				value |= (b & 0x7F) << index * 7;
			}
			return c(value);
		}

		if (t == byte[].class) {
			byte[] bytes = new byte[read(buf, int.class, false)];
			buf.readBytes(bytes);
			return c(bytes);
		}

		if (t == String.class)
			return c(new String(read(buf, byte[].class, false), StandardCharsets.UTF_8));

		if (t == UUID.class)
			return c(new UUID(buf.readLong(), buf.readLong()));

		if (t.isEnum())
			return t.getEnumConstants()[read(buf, int.class, false)];

		throw new UnsupportedOperationException("Encoding of " + t + " not implemented.");
	}

	public static <T> T read(ByteBuf buf, Field field) {
		if (List.class.isAssignableFrom(field.getType()))
			return c(IOUtil.gson.fromJson(read(buf, String.class, false), field.getGenericType()));

		return c(read(buf, field.getType(), field.isAnnotationPresent(FixedWidth.class)));
	}

	public static void write(ByteBuf buf, Object o, boolean fixedWidth) {
		if (o instanceof Integer) {
			int input = (int) o;
			if (fixedWidth) {
				buf.writeInt(input);
				return;
			}

			// encoded in 7 bit chunks, with the 8th bit indicating whether a chunk is following
			while ((input & 0xFFFFFF80) != 0) {
				buf.writeByte(input & 0x7F | 0x80);
				input >>>= 7;
			}
			buf.writeByte(input);
		}

		else if (o instanceof byte[]) {
			byte[] bytes = (byte[]) o;
			write(buf, bytes.length, false);
			buf.writeBytes(bytes);
		}

		else if (o instanceof String) {
			write(buf, ((String) o).getBytes(StandardCharsets.UTF_8), false);
		}

		else if (o instanceof UUID) {
			UUID uuid = (UUID) o;
			buf.writeLong(uuid.getMostSignificantBits());
			buf.writeLong(uuid.getLeastSignificantBits());
		}

		else if (o instanceof Enum<?>)
			write(buf, ((Enum<?>) o).ordinal(), false);

		else
			throw new UnsupportedOperationException("Encoding of " + o.getClass() + " not implemented.");
	}

	public static void write(ByteBuf buf, Object parent, Field field) throws IllegalAccessException {
		write(buf, field.get(parent), field.isAnnotationPresent(FixedWidth.class));
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface FixedWidth {}
}