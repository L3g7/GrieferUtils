/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.mysterymod_connection.packets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

/**
 * description missing.
 */
public class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws ReflectiveOperationException {
		int length = msg.readInt();
		if (length > 0) {
			byte packetId = msg.readByte();
			if (!Protocol.registry.containsKey(packetId)) {
				if (packetId != (byte) -97 && packetId != 99)
					System.err.println("[MysteryMod] Unknown packet id " + packetId + "!");
				return;
			}

			Packet packet = Protocol.registry.get(packetId).getConstructor().newInstance();
			if (packet.hasUuid)
				packet.uuid = Protocol.read(msg, UUID.class, false);

			for (Field field : packet.getClass().getDeclaredFields())
				field.set(packet, Protocol.read(msg, field));

			out.add(packet);
		}
	}

}
