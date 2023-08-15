/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

import dev.l3g7.griefer_utils.core.event_bus.Event;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.MysteryModConnection;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.Packet;
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