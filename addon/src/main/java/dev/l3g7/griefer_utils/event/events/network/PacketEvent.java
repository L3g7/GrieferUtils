/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.event.events.network;

import dev.l3g7.griefer_utils.core.event_bus.Event;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class PacketEvent<P extends Packet<?>> extends Event {

	public final P packet;

	private PacketEvent(P packet) {
		this.packet = packet;
	}

	public static class PacketReceiveEvent<P extends Packet<?>> extends PacketEvent<P> {

		public PacketReceiveEvent(P packet) {
			super(packet);
		}

		@Mixin(NetworkManager.class)
		private static class MixinNetworkManager {

			@Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
			public void injectChannelRead0(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
				if (new PacketReceiveEvent<>(packet).fire().isCanceled())
					ci.cancel();
			}

		}

	}

	public static class PacketSendEvent<P extends Packet<?>> extends PacketEvent<P> {

		public PacketSendEvent(P packet) {
			super(packet);
		}

		@Mixin(NetHandlerPlayClient.class)
		private static class MixinNetHandlerPlayClient {

			@Inject(method = "addToSendQueue", at = @At("HEAD"), cancellable = true)
			private void injectPacketSendEvent(Packet<?> packet, CallbackInfo ci) {
				if (new PacketSendEvent<>(packet).fire().isCanceled())
					ci.cancel();
			}

		}

	}

}
