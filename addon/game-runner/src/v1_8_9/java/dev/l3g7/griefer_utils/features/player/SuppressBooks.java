/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.init.Items;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

@Singleton
public class SuppressBooks extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Bücher unterdrücken")
		.description("Unterdrückt das Öffnen von Büchern durch GrieferGames.")
		.icon(Items.book);

	@EventListener
	private void onPacket(PacketReceiveEvent<S3FPacketCustomPayload> p) {
		if ("MC|BOpen".equals(p.packet.getChannelName()))
			p.cancel();
	}

}
