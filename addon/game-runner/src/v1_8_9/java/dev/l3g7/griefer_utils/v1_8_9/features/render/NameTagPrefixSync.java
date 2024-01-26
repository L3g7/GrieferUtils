/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.DisplayNameGetEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.TabListEvent;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.util.IChatComponent;

/**
 * Colors the player display name to match the selected prefix.
 */
@Singleton
public class NameTagPrefixSync extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Nametag mit Prefix")
		.description("Färbt den Namen über dem Kopf so, dass er zum ausgewählten Prefix passt.")
		.icon("rainbow_name");

	@EventListener
	public void onDisplayNameRender(DisplayNameGetEvent event) {
		IChatComponent component = TabListEvent.getCachedName(event.player.getUniqueID());
		if(component != null)
			event.displayName = component;
	}

}
