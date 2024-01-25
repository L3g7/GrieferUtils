/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.text_component_tweaks;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.ItemTooltipEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent.TabListNameUpdateEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.Material;
import net.minecraft.util.IChatComponent;

import static dev.l3g7.griefer_utils.core.event_bus.Priority.LOW;

abstract class TextComponentTweak extends Feature {

	final BooleanSetting chat = new BooleanSetting()
		.name("In Chat")
		.icon("speech_bubble")
		.defaultValue(true);

	final BooleanSetting tab = new BooleanSetting()
		.name("In Tabliste")
		.icon("tab_list")
		.defaultValue(true)
		.callback(TabListEvent::updatePlayerInfoList);

	final BooleanSetting item = new BooleanSetting()
		.name("In Item-Beschreibungen")
		.icon(Material.GOLD_INGOT)
		.defaultValue(true);

	@EventListener(priority = LOW)
	public void onTabList(TabListNameUpdateEvent event) {
		if (!tab.get())
			return;

		modify(event.component);
	}

	@EventListener(priority = LOW)
	public void onTooltip(ItemTooltipEvent e) {
		if (!item.get())
			return;

		e.toolTip.replaceAll(this::modify);
	}

	@EventListener(priority = LOW)
	public void onMessageModifyChat(MessageModifyEvent event) {
		if (!chat.get())
			return;

		modify(event.message);
	}

	abstract void modify(IChatComponent component);

	abstract String modify(String message);

}
