/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import net.labymod.api.client.chat.ChatMessage;
import net.labymod.api.client.chat.advanced.ChatMessagesWidget;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.render.draw.batch.BatchRectangleRenderer;
import net.labymod.api.client.render.font.ComponentRenderMeta;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.configuration.labymod.chat.AdvancedChatMessage;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.core.client.chat.DefaultChatController;
import net.labymod.core.client.chat.DefaultChatMessage;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static org.spongepowered.asm.mixin.injection.At.Shift.BEFORE;

public class ChatLineUtil {

	public static AdvancedChatMessage hoveredMessage;

	public static final List<IChatComponent> MODIFIED_COMPONENTS = new ArrayList<>();
	public static final List<IChatComponent> UNMODIFIED_COMPONENTS = new ArrayList<>();

	@EventListener
	private static void onMessageModify(MessageModifyEvent event) {
		UNMODIFIED_COMPONENTS.add(event.original);
	}

	public static IChatComponent getUnmodifiedIChatComponent(IChatComponent iChatComponent) {
		if (iChatComponent == null)
			return null;

		int index = MODIFIED_COMPONENTS.indexOf(iChatComponent);

		if (index == -1)
			return null;

		return UNMODIFIED_COMPONENTS.get(index);
	}

	public static IChatComponent getHoveredComponent() {
		if (hoveredMessage == null)
			return null;

		return (IChatComponent) hoveredMessage.component();
	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = ChatMessagesWidget.class, remap = false)
	private static class MixinChatMessagesWidget {

		@Shadow
		private ComponentRenderMeta lastHoveredComponentMeta;

		private ComponentRenderMeta lastHoveredRenderMeta = null;

		@Inject(method = "renderMessage", at = @At("TAIL"))
		private void injectRenderMessage(Stack stack, MutableMouse mouse, AdvancedChatMessage message, int lineIndex, BatchRectangleRenderer rectangleRenderer, int phase, CallbackInfoReturnable<Integer> cir) {
			if (lastHoveredComponentMeta == null
				|| lastHoveredRenderMeta == lastHoveredComponentMeta
				|| lastHoveredComponentMeta.getHovered().isEmpty())
				return;

			hoveredMessage = message;
			lastHoveredRenderMeta = lastHoveredComponentMeta;
		}

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = DefaultChatController.class, remap = false)
	private static class MixinDefaultChatController {

		@Inject(method = "addMessage(Lnet/labymod/api/client/chat/ChatMessage;Z)Lnet/labymod/api/client/chat/ChatMessage;", at = @At(value = "INVOKE", target = "Lnet/labymod/api/event/client/chat/ChatReceiveEvent;isCancelled()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
		private void injectAddMessageCancel(ChatMessage chatMessage, boolean justReceived, CallbackInfoReturnable<ChatMessage> cir, DefaultChatMessage message, ChatReceiveEvent event) {
			if (event.isCancelled())
				UNMODIFIED_COMPONENTS.remove(UNMODIFIED_COMPONENTS.size() - 1);
		}

		@Inject(method = "addMessage(Lnet/labymod/api/client/chat/ChatMessage;Z)Lnet/labymod/api/client/chat/ChatMessage;", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", shift = BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
		private void injectAddMessagePost(ChatMessage chatMessage, boolean justReceived, CallbackInfoReturnable<ChatMessage> cir, DefaultChatMessage message) {
			if (justReceived)
				MODIFIED_COMPONENTS.add((IChatComponent) message.component());
		}

	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = GuiChatAdapter.class, remap = false)
	private static class MixinGuiChatAdapter {

		@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/MessageData;getFilter()Lnet/labymod/ingamechat/tools/filter/Filters$Filter;"))
		public void postMessageModifiedEvent(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
			MODIFIED_COMPONENTS.add(component);
		}

	}

}
