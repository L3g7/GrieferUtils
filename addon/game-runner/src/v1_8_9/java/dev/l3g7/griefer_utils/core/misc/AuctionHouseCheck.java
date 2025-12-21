/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;

import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.LOWEST;

@Singleton
public class AuctionHouseCheck {

	private static boolean isChatLocked;

	public static boolean isChatLocked() {
		return isChatLocked;
	}

	@EventListener
	public void onMessage(MessageReceiveEvent event) {
		String message = event.message.getFormattedText().trim();
		if (message.equals("§r§8[§r§6GrieferGames§r§8] §r§7Bitte gib den gewünschten §r§eStartpreis§r§7 ein:§r"))
			isChatLocked = true;
	}

	@EventListener(priority = LOWEST)
	public void onMessage(MessageSendEvent event) {
		isChatLocked = false;
	}

	@EventListener
	public void onServerSwitch(ServerSwitchEvent event) {
		isChatLocked = false;
	}

}
