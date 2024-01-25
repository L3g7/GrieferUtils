/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.event.events.render;

import dev.l3g7.griefer_utils.core.event_bus.Event;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class ChatEvent extends Event {

	public static class ChatLineInitEvent extends ChatEvent {

		public final IChatComponent component;

		public ChatLineInitEvent(IChatComponent component) {
			this.component = component;
		}

	}

	public static class ChatMessageAddEvent extends ChatEvent {

		public final IChatComponent component;

		public ChatMessageAddEvent(IChatComponent component) {
			this.component = component;
		}

	}

	@Mixin(value = GuiChatAdapter.class, remap = false)
	private static class MixinGuiChatAdapter {

		@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/ChatRenderer;getVisualWidth()I"))
		public void postChatLineInitEvent(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
			new ChatEvent.ChatLineInitEvent(component).fire();

			if (!refresh)
				new ChatEvent.ChatMessageAddEvent(component).fire();
		}

	}

}