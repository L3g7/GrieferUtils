/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events.network;

import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.MysteryModConnection;
import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.Packet;
import io.netty.channel.ChannelHandlerContext;

public abstract class MysteryModConnectionEvent extends Event {

	public final ChannelHandlerContext ctx;

	private MysteryModConnectionEvent(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public static class MMPacketReceiveEvent<P extends Packet> extends MysteryModConnectionEvent {

		public final P packet;

		public MMPacketReceiveEvent(ChannelHandlerContext ctx, P packet) {
			super(ctx);
			this.packet = packet;
		}

	}

	public static class MMStateChangeEvent extends MysteryModConnectionEvent {

		public final MysteryModConnection.State state;

		public MMStateChangeEvent(ChannelHandlerContext ctx, MysteryModConnection.State state) {
			super(ctx);
			this.state = state;
		}

	}
}