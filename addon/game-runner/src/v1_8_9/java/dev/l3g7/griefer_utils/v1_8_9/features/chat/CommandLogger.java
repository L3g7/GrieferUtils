/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.Priority;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class CommandLogger extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Befehle loggen")
		.description("Schreibt die gesendeten Befehle in den Log.")
		.icon(Blocks.command_block);

	private final Logger logger = LogManager.getLogger("CommandLogger");

	@EventListener(priority = Priority.LOWEST)
	private void onMessageSend(PacketEvent.PacketSendEvent<C01PacketChatMessage> event) {
		if (event.packet.getMessage().startsWith("/"))
			logger.info("executed " + event.packet.getMessage());
	}

}
