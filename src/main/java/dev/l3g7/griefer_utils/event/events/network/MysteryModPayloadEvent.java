/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.event.events.network;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent.PacketReceiveEvent;
import net.labymod.utils.JsonParse;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

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
	private static void onPacket(PacketReceiveEvent event) {
		if (!(event.packet instanceof S3FPacketCustomPayload))
			return;

		S3FPacketCustomPayload packet = (S3FPacketCustomPayload) event.packet;
		if (!packet.getChannelName().equals("mysterymod:mm"))
			return;

		PacketBuffer data = packet.getBufferData();
		data.markReaderIndex();
		MinecraftForge.EVENT_BUS.post(new MysteryModPayloadEvent(data.readStringFromBuffer(65536), JsonParse.parse(data.readStringFromBuffer(65536))));
		data.resetReaderIndex();
	}

}
