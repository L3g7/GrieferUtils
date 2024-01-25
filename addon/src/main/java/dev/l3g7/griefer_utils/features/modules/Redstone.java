/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.Material;

@Singleton
public class Redstone extends Module {

	private int redstoneState = -1;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Redstone")
		.description("Zeigt dir den Redstonestatus an.")
		.icon(Material.REDSTONE);

	@EventListener(triggerWhenDisabled = true)
	public void onMMCustomPayload(MysteryModPayloadEvent event) {
		if (!event.channel.equals("redstone"))
			return;

		redstoneState = event.payload.getAsJsonObject().get("status").getAsInt();
	}

	@Override
	public String[] getValues() {
		if (mc.theWorld == null)
			return getDefaultValues();

		switch (redstoneState) {
			case -1:
				return new String[]{"Unbekannt"};
			case 0:
				return new String[]{"§aAktiviert"};
			default:
				return new String[]{"§4Deaktiviert"};
		}
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"Unbekannt"};
	}
}