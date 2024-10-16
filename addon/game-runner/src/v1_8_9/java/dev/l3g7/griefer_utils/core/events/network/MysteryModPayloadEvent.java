/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events.network;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.io.StringReader;

/**
 * An event being posted when a {@link S3FPacketCustomPayload} on the {@code mysterymod:mm} channel is received.
 */
public class MysteryModPayloadEvent extends Event {

	public final String channel;
	public final JsonElement payload;

	private MysteryModPayloadEvent(String channel, JsonElement payload) {
		this.channel = channel;
		this.payload = payload;
	}

	@EventListener
	private static void onPacket(PacketReceiveEvent<S3FPacketCustomPayload> event) {
		if (!event.packet.getChannelName().equals("mysterymod:mm"))
			return;

		PacketBuffer data = event.packet.getBufferData();
		data.markReaderIndex();
		new MysteryModPayloadEvent(data.readStringFromBuffer(65536), Streams.parse(new JsonReader(new StringReader(data.readStringFromBuffer(65536))))).fire();
		data.resetReaderIndex();
	}

}
