/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.features.modules.Laby4Module;
import net.minecraft.init.Items;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
@ExclusiveTo(LABY_4)
public class Redstone extends Laby4Module {

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
		if (mc().theWorld == null)
			return "Unbekannt";

		return switch (redstoneState) {
			case -1 -> "Unbekannt";
			case 0 -> "§aAktiviert";
			default -> "§4Deaktiviert";
		};
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
	}

}
