/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events.network;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.network.LabyModNeoPayloadEvent.LabyModNeoPacket;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.ChatComponentText;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.LOW;

/**
 * An event being posted when a {@link S3FPacketCustomPayload} on the {@code labymod:neo} channel is received.
 */
public class LabyModNeoPayloadEvent<T extends LabyModNeoPacket> extends Event {

	public final T packet;

	private LabyModNeoPayloadEvent(T packet) {
		this.packet = packet;
	}

	@EventListener
	private static void onPacket(PacketReceiveEvent<S3FPacketCustomPayload> event) {
		if (!event.packet.getChannelName().equals("labymod:neo"))
			return;

		PacketBuffer data = event.packet.getBufferData();
		data.markReaderIndex();

		int id = data.readVarIntFromBuffer();
		if (id == 11)
			new LabyModNeoPayloadEvent<>(SubtitlePacket.decode(data)).fire();

		data.resetReaderIndex();
	}

	@ExclusiveTo(LABY_3)
	private static class Init {

		@EventListener(priority = LOW)
		private static void register(PacketReceiveEvent<S3FPacketCustomPayload> event) {
			if (!event.packet.getChannelName().equals("MC|Brand"))
				return;

			// Register labymod:neo connection
			PacketBuffer register = new PacketBuffer(Unpooled.wrappedBuffer("labymod:neo".getBytes(StandardCharsets.UTF_8)));
			event.manager.sendPacket(new C17PacketCustomPayload("REGISTER", register));

			// Init labymod:neo connection
			PacketBuffer data = new PacketBuffer(Unpooled.buffer());
			data.writeVarIntToBuffer(0);
			data.writeString("4.3.54");

			event.manager.sendPacket(new C17PacketCustomPayload("labymod:neo", data));
		}
	}

	protected static abstract class LabyModNeoPacket {

		protected static ChatComponentText readComponent(PacketBuffer data) {
			byte id = data.readByte();

			// Read text
			ChatComponentText component;
			if (id == 1)
				component = new ChatComponentText(data.readStringFromBuffer(Short.MAX_VALUE));
			else
				component = new ChatComponentText("");

			// Read color & decorations
			if (data.readBoolean()) { // is decorated
				if (data.readBoolean()) // has color
					data.readInt(); // not implemented

				// Read decorations
				int setDecorations = data.readVarIntFromBuffer();
				for (int i = 0; i < setDecorations; i++) {
					int ordinal = data.readByte();
					switch (ordinal) {
						case 0 -> component.getChatStyle().setObfuscated(data.readBoolean());
						case 1 -> component.getChatStyle().setBold(data.readBoolean());
						case 2 -> component.getChatStyle().setStrikethrough(data.readBoolean());
						case 3 -> component.getChatStyle().setUnderlined(data.readBoolean());
						case 4 -> component.getChatStyle().setItalic(data.readBoolean());
					}
				}
			}

			// Read children
			int children = data.readVarIntFromBuffer();
			for (int i = 0; i < children; i++)
				component.appendSibling(readComponent(data));

			return component;
		}

	}

	/**
	 * @see net.labymod.serverapi.core.packet.clientbound.game.display.SubtitlePacket
	 */
	public static class SubtitlePacket extends LabyModNeoPacket {

		public final List<Subtitle> subtitles;

		private SubtitlePacket(List<Subtitle> subtitles) {
			this.subtitles = subtitles;
		}

		public record Subtitle(UUID uuid, String text, double scale) {}

		public static SubtitlePacket decode(PacketBuffer data) {
			int length = data.readVarIntFromBuffer();
			List<Subtitle> subtitles = new ArrayList<>(length);

			for (int i = 0; i < length; i++) {
				UUID uuid = data.readUuid();
				boolean hasText = data.readBoolean();
				if (!hasText) {
					subtitles.add(new Subtitle(uuid, null, 1));
					continue;
				}

				// Read component
				ChatComponentText text = readComponent(data);
				double scale = data.readDouble();
				subtitles.add(new Subtitle(uuid, text.getFormattedText(), scale));
			}

			return new SubtitlePacket(subtitles);
		}

	}

}
