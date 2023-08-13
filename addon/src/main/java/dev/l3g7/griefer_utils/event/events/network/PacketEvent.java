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

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class PacketEvent extends Event {

	public final Packet<?> packet;

	private PacketEvent(Packet<?> packet) {
		this.packet = packet;
	}

	@Cancelable
	public static class PacketReceiveEvent extends PacketEvent {

		public PacketReceiveEvent(Packet<?> packet) {
			super(packet);
		}

		@Mixin(NetworkManager.class)
		private static class MixinNetworkManager {

			@Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
			public void injectChannelRead0(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
				if (MinecraftForge.EVENT_BUS.post(new PacketReceiveEvent(packet)))
					ci.cancel();
			}

		}

	}

	@Cancelable
	public static class PacketSendEvent extends PacketEvent {

		public PacketSendEvent(Packet<?> packet) {
			super(packet);
		}

		@Mixin(NetHandlerPlayClient.class)
		private static class MixinNetHandlerPlayClient {

			@Inject(method = "addToSendQueue", at = @At("HEAD"), cancellable = true)
			private void injectPacketSendEvent(Packet<?> packet, CallbackInfo ci) {
				if (MinecraftForge.EVENT_BUS.post(new PacketSendEvent(packet)))
					ci.cancel();
			}

		}

	}

}
