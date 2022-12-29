package dev.l3g7.griefer_utils.event.events.network;

import dev.l3g7.griefer_utils.util.misc.mysterymod_connection.MysteryModConnection;
import dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.Packet;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.eventhandler.Event;

public class MysteryModConnectionEvent extends Event {

	public final ChannelHandlerContext ctx;

	private MysteryModConnectionEvent(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public static class MMPacketReceiveEvent extends MysteryModConnectionEvent {

		public final Packet packet;

		public MMPacketReceiveEvent(ChannelHandlerContext ctx, Packet packet) {
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