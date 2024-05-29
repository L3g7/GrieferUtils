/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events;

import dev.l3g7.griefer_utils.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageBridge.messageBridge;

/**
 * A forge event for message processing.
 */
public class MessageEvent extends Event {

	public static class MessageModifyEvent extends MessageEvent {

		public final IChatComponent original;
		public IChatComponent message;
		private boolean modified = false;

		public MessageModifyEvent(IChatComponent original, IChatComponent message) {
			this.original = original;
			this.message = message.createCopy();
		}

		@OnEnable
		private static void register() {
			LabyBridge.labyBridge.onMessageModify((prevMsg, newMsg) -> {
				MessageModifyEvent msg = new MessageModifyEvent(
					messageBridge.fromLaby(prevMsg),
					messageBridge.fromLaby(newMsg)
				);
				msg.fire();
				return msg.modified ? messageBridge.toLaby(msg.message) : prevMsg;
			});
		}

		public void setMessage(IChatComponent message) {
			this.message = message;
			modified = true;
		}

	}

	public static class MessageSendEvent extends MessageEvent {

		public static boolean post(String message) {
			if (!new MessageSendEvent(message).fire().isCanceled())
				return LabyBridge.labyBridge.trySendMessage(message);

			return true;
		}

		public final String message;

		private MessageSendEvent(String message) {
			this.message = message;
		}

		@OnEnable
		private static void register() {
			LabyBridge.labyBridge.onMessageSend(message -> new MessageSendEvent(message).fire().isCanceled());
		}

	}

	public static class MessageAboutToBeSentEvent extends MessageEvent {

		public final String message;

		public MessageAboutToBeSentEvent(String message) {
			this.message = message;
		}

		@Mixin(EntityPlayerSP.class)
		private static class MixinEntityPlayerSP {

			@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
			public void injectSendChatMessage(String message, CallbackInfo ci) {
				if (new MessageEvent.MessageAboutToBeSentEvent(message).fire().isCanceled())
					ci.cancel();
			}

		}

	}

	public static class MessageReceiveEvent extends MessageEvent {

		public final IChatComponent message;
		public final byte type;

		public MessageReceiveEvent(IChatComponent message, byte type) {
			this.message = message;
			this.type = type;
		}

		@EventListener
		private static void onPacketReceive(PacketEvent.PacketReceiveEvent<S02PacketChat> event) {
			if (new MessageReceiveEvent(event.packet.getChatComponent(), event.packet.getType()).fire().isCanceled())
				event.cancel();
		}

	}

	@Bridged
	public interface MessageBridge {

		MessageBridge messageBridge = FileProvider.getBridge(MessageBridge.class);

		IChatComponent fromLaby(Object message);
		Object toLaby(IChatComponent message);

	}

}
