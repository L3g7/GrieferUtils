/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.event.events.render;

import de.emotechat.addon.gui.chat.render.EmoteChatRenderer;
import dev.l3g7.griefer_utils.core.event_bus.Event;
import net.labymod.ingamechat.renderer.ChatLine;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

public class RenderChatEvent extends Event {

	public final ChatLine chatLine;
	public final int y;
	public final float alpha;

	public RenderChatEvent(ChatLine chatLine, int y, float alpha) {
		this.chatLine = chatLine;
		this.y = y;
		this.alpha = alpha;
	}

	@Mixin(value = EmoteChatRenderer.class, remap = false)
	private static class MixinEmoteChatRenderer {

		@Inject(method = "drawLine", at = @At("TAIL"))
		@SuppressWarnings("InvalidInjectorMethodSignature") // Library is obfuscated, so method parameters are as well
		public void injectDrawLine(FontRenderer font, ChatLine chatLine, float x, float y, int width, int alpha, int mouseX, int mouseY, CallbackInfo ci) {
			new RenderChatEvent(chatLine, (int) y + 8, alpha / 255f).fire();
		}

	}

	@Mixin(value = ChatRenderer.class, remap = false)
	private static class MixinChatRenderer {

		@Inject(method = "renderChat", at = @At(value = "INVOKE", target = "Lnet/labymod/utils/DrawUtils;drawStringWithShadow(Ljava/lang/String;DDI)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
		private void injectRenderChat(int updateCounter, CallbackInfo ci, DrawUtils draw, int fontHeight, float scale, int chatLineCount, boolean chatOpen, float opacity, int width, int visibleMessages, double totalMessages, double animationSpeed, float lineHeight, double shift, double posX, double posY, int i, Iterator<ChatLine> chatLineIterator, ChatLine chatline, boolean firstLine, boolean lastLine, int updateCounterDifference, int alpha, int x, int y) {
			new RenderChatEvent(chatline, y, alpha / 255f).fire();
		}

	}



}
