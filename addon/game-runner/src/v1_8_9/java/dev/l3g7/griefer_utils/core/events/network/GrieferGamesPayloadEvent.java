/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events.network;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import org.jetbrains.annotations.CheckReturnValue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static dev.l3g7.griefer_utils.core.api.util.Util.elevate;

/**
 * An event being posted when a {@link S3FPacketCustomPayload} on the {@code griefergames:main} channel is received.
 */
public class GrieferGamesPayloadEvent extends Event {

	public final String channel;
	public final byte[] payload;

	private GrieferGamesPayloadEvent(String channel, byte[] payload) {
		this.channel = channel;
		this.payload = payload;
	}

	@CheckReturnValue
	public DataInputStream createStream() {
		return new DataInputStream(new ByteArrayInputStream(payload));
	}

	@EventListener
	private static void onPacket(PacketReceiveEvent<S3FPacketCustomPayload> event) {
		if (!event.packet.getChannelName().equals("griefergames:main"))
			return;

		PacketBuffer data = event.packet.getBufferData();
		data.markReaderIndex();

		try (DataInputStream in = new DataInputStream(new ByteBufInputStream(data))) {
			String id = in.readUTF();

			byte[] payload = new byte[data.readableBytes()];
			data.readBytes(payload);

			new GrieferGamesPayloadEvent(id, payload).fire();
		} catch (IOException e) {
			throw elevate(e);
		}

		data.resetReaderIndex();
	}

}
