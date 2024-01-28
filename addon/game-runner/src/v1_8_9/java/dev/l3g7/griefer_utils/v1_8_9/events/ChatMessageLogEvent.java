/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events;

import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.minecraft.client.gui.GuiNewChat;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.*;

public class ChatMessageLogEvent extends Event {

	public String message;

	public ChatMessageLogEvent(String message) {
		this.message = message;
	}

	public static void tryLogging(Logger logger, String message) {
		ChatMessageLogEvent event = new ChatMessageLogEvent(message);

		if (!event.fire().isCanceled())
			logger.info(event.message);
	}

	@Mixin(GuiNewChat.class)
	@ExclusiveTo({LABY_4, ANY_MINECRAFT})
	private static class MixinGuiNewChat {

		@Redirect(method = "printChatMessageWithOptionalDeletion", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V"))
		public void log(Logger logger, String message) {
			tryLogging(logger, message);
		}

	}

	@Mixin(value = GuiChatAdapter.class, remap = false)
	@ExclusiveTo({LABY_3, ANY_MINECRAFT})
	private static class MixinGuiChatAdapter {

		@Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V"))
		public void log(Logger logger, String message) {
			tryLogging(logger, message);
		}

	}

}
