/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * description missing.
 */
public class PacketEncoder extends MessageToMessageEncoder<Packet> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet packet, List<Object> out) throws ReflectiveOperationException {
		ByteBuf buf = ctx.alloc().buffer();
		Optional<Map.Entry<Byte, Class<? extends Packet>>> packetId = Protocol.registry.entrySet().stream()
			.filter(e -> e.getValue() == packet.getClass())
			.findAny();
		if (!packetId.isPresent())
			throw new UnsupportedOperationException("[MysteryMod] packet " + packet.getClass() + " is not registered!");

		buf.writeByte(packetId.get().getKey());
		if (packet.hasUuid)
			Protocol.write(buf, packet.uuid != null ? packet.uuid : UUID.randomUUID(), false);

		for (Field field : packet.getClass().getDeclaredFields()) {
			Protocol.write(buf, packet, field);
		}

		out.add(buf);
	}

}
