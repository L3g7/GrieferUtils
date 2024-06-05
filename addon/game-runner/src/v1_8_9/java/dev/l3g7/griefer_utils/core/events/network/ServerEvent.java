/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events.network;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.api.event.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event.event_bus.Priority;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * An event related to the server connection.
 */
public class ServerEvent extends Event {

	private static boolean hotfix_brandPacketReceived = true, // TODO
		hotfix_watching = true; // only listen to first MC|Brand packet after quit to avoid multiple detections when replaying the packet

	public static class ServerSwitchEvent extends ServerEvent {

		@EventListener
		private static void onPacket(PacketReceiveEvent<S3FPacketCustomPayload> event) {
			if (event.packet.getChannelName().equals("MC|Brand"))
				new ServerSwitchEvent().fire();
		}

	}

	public static class ServerJoinEvent extends ServerEvent {

		@OnEnable
		private static void register() {
			LabyBridge.labyBridge.onJoin(() -> new ServerJoinEvent().fire());
		}

	}

	public static class GrieferGamesJoinEvent extends ServerEvent {

		@EventListener(priority = Priority.HIGHEST)
		private static void onPacketReceive(PacketReceiveEvent<S3FPacketCustomPayload> event) {
			if (hotfix_watching && event.packet.getChannelName().equals("MC|Brand")) {
				hotfix_brandPacketReceived = true;
				hotfix_watching = false;
			}

			else if (hotfix_brandPacketReceived && event.packet.getChannelName().equals("mysterymod:mm")) {
				hotfix_brandPacketReceived = false;
				new GrieferGamesJoinEvent().fire();
			}
		}

	}

	public static class ServerQuitEvent extends ServerEvent {

		@OnEnable
		private static void register() {
			LabyBridge.labyBridge.onQuit(() -> {
				hotfix_brandPacketReceived = false;
				hotfix_watching = true;
				new ServerQuitEvent().fire();
			});
		}

	}

}
