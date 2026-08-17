/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Supplier;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.client.component.Component;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.main.LabyMod;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static net.labymod.api.Laby.labyAPI;

/**
 * A forge event for message processing.
 */
public class MessageEvent extends Event {

	public static class MessageModifyEvent extends MessageEvent {

		public final IChatComponent original;
		public IChatComponent message;

		public MessageModifyEvent(IChatComponent original, IChatComponent message) {
			this.original = original;
			this.message = message.createCopy();
		}

		public void setMessage(IChatComponent message) {
			this.message = message;
		}

		// See Laby3MessageModifyEventRegistrar

		@ExclusiveTo(LABY_4)
		private static class Laby4Registrar {
			@OnEnable
			private static void register() {
				Laby4Util.register(ChatReceiveEvent.class, v -> {
					MessageModifyEvent event = new MessageModifyEvent((IChatComponent) v.chatMessage().originalComponent(), (IChatComponent) v.message());
					event.fire();
					if (event.message != null)
						v.setMessage((Component) event.message);
				});
			}
		}

	}

	public static class MessageSendEvent extends MessageEvent {

		public static boolean post(String message) {
			if (new MessageSendEvent(message).fire().isCanceled())
				return true;

			if (LabyBridge.labyBridge.forge()) {
				// Wrap in supplier to not cause problems when forge doesn't exist
				Supplier<Integer> runCommand = () -> Reflection.invoke(ClientCommandHandler.instance, "executeCommand", player(), message);
				if (runCommand.get() != 0)
					return true;
			}

			// Fire LabyMod's events
			return LabyBridge.dispatchGet(() -> {
				for (net.labymod.api.events.MessageSendEvent lmEvent : LabyMod.getInstance().getEventManager().getMessageSend())
					if (lmEvent.onSend(message))
						return true;

				return false;
			}, () -> {
				ChatMessageSendEvent event = new ChatMessageSendEvent(message, false);
				labyAPI().eventBus().fire(event);
				return event.isCancelled();
			});
		}

		public final String message;

		private MessageSendEvent(String message) {
			this.message = message;
		}

		@ExclusiveTo(LABY_3)
		private static class Laby3Registrar {
			@OnEnable
			private static void register() {
				LabyMod.getInstance().getEventManager().register((net.labymod.api.events.MessageSendEvent) message -> {
					if (message == null)
						return false;

					return new MessageSendEvent(message).fire().isCanceled();
				});
			}
		}

		@ExclusiveTo(LABY_4)
		private static class Laby4Registrar {
			@OnEnable
			private static void register() {
				Laby4Util.register(ChatMessageSendEvent.class, v -> {
					if (v.isCancelled())
						return;

					String message = v.getMessage();
					if (message != null)
						v.setCancelled(new MessageSendEvent(message).fire().isCanceled());
				});
			}
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
				if (message != null && !message.trim().isEmpty() && new MessageEvent.MessageAboutToBeSentEvent(message).fire().isCanceled())
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

}
