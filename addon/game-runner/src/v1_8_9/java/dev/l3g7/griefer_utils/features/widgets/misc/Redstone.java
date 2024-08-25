/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.misc;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.SimpleWidget;
import net.minecraft.init.Items;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

@Singleton
public class Redstone extends SimpleWidget {

	private int redstoneState = -1;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Redstone")
		.description("Zeigt dir den Redstonestatus an.")
		.icon(Items.redstone);

	@EventListener(triggerWhenDisabled = true)
	public void onMMCustomPayload(MysteryModPayloadEvent event) {
		if (!event.channel.equals("redstone"))
			return;

		redstoneState = event.payload.getAsJsonObject().get("status").getAsInt();
	}

	@Override
	public String getValue() {
		if (world() == null)
			return "Unbekannt";

		return switch (redstoneState) {
			case -1 -> "Unbekannt";
			case 0 -> "§aAktiviert";
			default -> "§4Deaktiviert";
		};
	}

}
