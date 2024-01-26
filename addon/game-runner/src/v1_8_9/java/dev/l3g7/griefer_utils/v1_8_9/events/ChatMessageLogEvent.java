/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events;

import dev.l3g7.griefer_utils.api.event.event_bus.Event;

public class ChatMessageLogEvent extends Event {

	public String message;

	public ChatMessageLogEvent(String message) {
		this.message = message;
	}

	/*
	// TODO: @Mixin(value = GuiChatAdapter.class, remap = false)
	private static class MixinGuiChatAdapter {

		@Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V"))
		public void log(Logger logger, String message) {
			ChatMessageLogEvent event = new ChatMessageLogEvent(message);

			if (!event.fire().isCanceled())
				logger.info(event.message);
		}

	}*/

}
