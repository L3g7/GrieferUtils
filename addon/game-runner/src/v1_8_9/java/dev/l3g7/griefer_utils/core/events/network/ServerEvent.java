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
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.main.LabyMod;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

/**
 * An event related to the server connection.
 */
public class ServerEvent extends Event {

	public static class ServerSwitchEvent extends ServerEvent {

		@EventListener
		private static void onPacket(PacketReceiveEvent<S3FPacketCustomPayload> event) {
			if (event.packet.getChannelName().equals("MC|Brand"))
				new ServerSwitchEvent().fire();
		}

	}

	public static class ServerJoinEvent extends ServerEvent {

		@ExclusiveTo(LABY_3)
		private static class Laby3Registrar {
			@OnEnable
			private static void register() {
				LabyMod.getInstance().getEventManager().registerOnJoin(v -> new ServerJoinEvent().fire());
			}
		}

		@ExclusiveTo(LABY_4)
		private static class Laby4Registrar {
			@OnEnable
			private static void register() {
				Laby4Util.register(net.labymod.api.event.client.network.server.ServerJoinEvent.class, v -> new ServerJoinEvent().fire());
			}
		}

	}

	public static class GrieferGamesJoinEvent extends ServerEvent {

		private static JoinState state = JoinState.START;

		@EventListener(priority = Priority.HIGHEST)
		private static void onServerJoin(ServerJoinEvent event) {
			state = JoinState.START;
		}

		@EventListener(priority = Priority.HIGHEST)
		private static void onPacketReceive(PacketReceiveEvent<S3FPacketCustomPayload> event) {
			if (state == JoinState.START && event.packet.getChannelName().equals("MC|Brand")) {
				state = JoinState.BRAND;
			} else if (state == JoinState.BRAND
				&& (event.packet.getChannelName().equals("mysterymod:mm") || event.packet.getChannelName().equals("griefergames:main"))) {
				state = JoinState.JOINED;
				new GrieferGamesJoinEvent().fire();
			}
		}

		private enum JoinState {
			/**
			 * State after the connection was established.
			 */
			START,

			/**
			 * State after the server sent an MC|Brand packet.
			 */
			BRAND,

			/**
			 * State after the server join has been acknowledged.
			 */
			JOINED
		}
	}

	public static class ServerQuitEvent extends ServerEvent {

		@ExclusiveTo(LABY_3)
		private static class Laby3Registrar {
			@OnEnable
			private static void register() {
				LabyMod.getInstance().getEventManager().registerOnQuit(v -> new ServerQuitEvent().fire());
			}
		}

		@ExclusiveTo(LABY_4)
		private static class Laby4Registrar {
			@OnEnable
			private static void register() {
				Laby4Util.register(ServerDisconnectEvent.class, v -> new ServerQuitEvent().fire());
			}
		}

	}

}
