/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.bridges;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import net.labymod.utils.manager.TagManager;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

/**
 * Handles firing of MessageModifyEvents.
 */
@ExclusiveTo(LABY_3)
public class Laby3MessageModifyEventRegistrar {

	public static IChatComponent fireEvent(IChatComponent original, IChatComponent modified) {
		return ((MessageModifyEvent) new MessageModifyEvent(original, modified).fire()).message;
	}

	private static final LinkedHashMap<ComponentHash, IChatComponent> unmodifiedChatComponents = new LinkedHashMap<>() {
		protected boolean removeEldestEntry(Map.Entry<ComponentHash, IChatComponent> eldest) {
			return size() > 100;
		}
	};

	@ExclusiveTo(LABY_3)
	@Mixin(value = TagManager.class, remap = false)
	public static class TagManagerMixin {

		@Unique
		private static IChatComponent grieferUtils$currentComponent;

		@Inject(method = "tagComponent", at = @At("HEAD"))
		private static void injectTagComponentHEAD(Object chatComponent, CallbackInfoReturnable<Object> cir) {
			grieferUtils$currentComponent = (IChatComponent) chatComponent;
		}

		@Inject(method = "tagComponent", at = @At("RETURN"), cancellable = true)
		private static void injectTagComponent(Object chatComponent, CallbackInfoReturnable<Object> cir) {
			IChatComponent original = unmodifiedChatComponents.remove(new ComponentHash(grieferUtils$currentComponent));
			cir.setReturnValue(fireEvent(original == null ? (IChatComponent) chatComponent : original, (IChatComponent) cir.getReturnValue()));
		}

	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = NetHandlerPlayClient.class)
	public static class NetHandlerPlayClientMixin {

		@Unique
		private IChatComponent grieferUtils$currentComponent;

		@Inject(method = "handleChat", at = @At("HEAD"))
		private void injectTagComponent(S02PacketChat packet, CallbackInfo ci) {
			grieferUtils$currentComponent = packet.getChatComponent();
		}

		@ModifyArg(method = "handleChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;printChatMessage(Lnet/minecraft/util/IChatComponent;)V"))
		private IChatComponent injectTagComponent(IChatComponent chatComponent) {
			unmodifiedChatComponents.put(new ComponentHash(chatComponent), grieferUtils$currentComponent);
			return chatComponent;
		}

	}

	/**
	 * Wrapper around IChatComponent to use identity hash codes instead of IChatComponent's hashCode method.
	 */
	@ExclusiveTo(LABY_3)
	public static class ComponentHash {

		private final IChatComponent component;

		public ComponentHash(IChatComponent component) {
			this.component = component;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(component);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof ComponentHash ch && component == ch.component;
		}

	}
}
