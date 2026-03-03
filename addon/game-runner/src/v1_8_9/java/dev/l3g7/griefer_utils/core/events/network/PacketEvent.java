/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events.network;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.injection.InheritedInvoke;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.util.IThreadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public abstract class PacketEvent<P extends Packet<?>> extends Event {

	public final P packet;
	public final NetworkManager manager;

	private PacketEvent(P packet, NetworkManager manager) {
		this.packet = packet;
		this.manager = manager;
	}

	public static class PacketReceiveEvent<P extends Packet<?>> extends PacketEvent<P> {

		public PacketReceiveEvent(P packet, NetworkManager manager) {
			super(packet, manager);
		}

		@Mixin(NetworkManager.class)
		private static class MixinNetworkManager {

			@Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
			public void injectChannelRead0(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
				if (new PacketReceiveEvent<>(packet, c(this)).fire().isCanceled())
					ci.cancel();
			}

		}

	}

	/**
	 * Fired after a packet was processed.
	 */
	public static class PacketReceivedEvent<P extends Packet<?>> extends PacketEvent<P> {

		public static Packet<?> LAST_QUEUED_PACKET;

		public PacketReceivedEvent(P packet, NetworkManager manager) {
			super(packet, manager);
		}

		@Mixin(NetworkManager.class)
		private static class MixinNetworkManager {

			@Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;processPacket(Lnet/minecraft/network/INetHandler;)V", shift = At.Shift.AFTER))
			public void injectChannelRead0(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
				new PacketReceivedEvent<>(packet, c(this)).fire();
			}

		}

		@Mixin(PacketThreadUtil.class)
		private static class MixinPacketThreadUtil {

			@Inject(method = "checkThreadAndEnqueue", at = @At("HEAD"))
			private static <T extends INetHandler> void onCheckThreadAndEnqueue(Packet<T> lvt_0_1_, T lvt_1_1_, IThreadListener lvt_2_1_, CallbackInfo ci) {
				LAST_QUEUED_PACKET = lvt_0_1_;
			}

		}

		@Mixin(targets = "net.minecraft.network.PacketThreadUtil$1")
		private static class MixinPacketThreadUtil$1 {

			@InheritedInvoke(Runnable.class)
			@Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;processPacket(Lnet/minecraft/network/INetHandler;)V", shift = At.Shift.AFTER))
			private void injectCheckThreadAndEnqueue(CallbackInfo ci) {
				new PacketReceivedEvent<>(LAST_QUEUED_PACKET, null).fire();
			}

		}

	}

	public static class PacketSendEvent<P extends Packet<?>> extends PacketEvent<P> {
		public PacketSendEvent(P packet, NetworkManager manager) {
			super(packet, manager);
		}

		@Mixin(NetHandlerPlayClient.class)
		private static class MixinNetHandlerPlayClient {

			@Shadow
			@Final
			private NetworkManager netManager;

			@Inject(method = "addToSendQueue", at = @At("HEAD"), cancellable = true)
			private void injectPacketSendEvent(Packet<?> packet, CallbackInfo ci) {
				if (new PacketSendEvent<>(packet, netManager).fire().isCanceled())
					ci.cancel();
			}

		}

	}

}
