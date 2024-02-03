/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc;

import dev.l3g7.griefer_utils.api.bridges.Bridge;
import net.labymod.api.client.chat.advanced.ChatMessagesWidget;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.render.draw.batch.BatchRectangleRenderer;
import net.labymod.api.client.render.font.ComponentRenderMeta;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.configuration.labymod.chat.AdvancedChatMessage;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;

public class HoveredMessageHandler {

	public static AdvancedChatMessage hoveredMessage;

	public static IChatComponent getMessage() {
		if (hoveredMessage == null)
			return null;

		return (IChatComponent) hoveredMessage.component();
	}

	public static IChatComponent getOriginalMessage() {
		if (hoveredMessage == null)
			return null;

		return (IChatComponent) hoveredMessage.originalComponent();
	}


	@Bridge.ExclusiveTo(LABY_4)
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

			HoveredMessageHandler.hoveredMessage = message;
			lastHoveredRenderMeta = lastHoveredComponentMeta;
		}

	}

}
