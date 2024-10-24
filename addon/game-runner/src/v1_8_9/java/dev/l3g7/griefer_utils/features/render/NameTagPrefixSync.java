/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.DisplayNameGetEvent;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.util.IChatComponent;

import java.util.ListIterator;

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
		if (component == null)
			return;

		if (event.displayName.getSiblings().size() == 0) {
			event.displayName = component;
			return;
		}

		// displayName has other stuff in front of /
		ListIterator<IChatComponent> it = event.displayName.getSiblings().listIterator();
		while (it.hasNext()) {
			if (it.next().getUnformattedText().contains(" \u2503 ")) {
				it.set(component);
				return;
			}
		}
	}

}
