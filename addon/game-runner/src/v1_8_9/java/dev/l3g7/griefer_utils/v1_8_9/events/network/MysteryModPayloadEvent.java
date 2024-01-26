/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent.PacketReceiveEvent;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

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
		new MysteryModPayloadEvent(data.readStringFromBuffer(65536), JsonParser.parseString(data.readStringFromBuffer(65536))).fire();
		data.resetReaderIndex();
	}

}
