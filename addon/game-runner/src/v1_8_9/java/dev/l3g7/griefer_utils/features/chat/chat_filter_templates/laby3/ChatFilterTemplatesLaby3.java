/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_filter_templates.laby3;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.features.chat.chat_filter_templates.ChatFilterTemplates;
import net.labymod.ingamechat.tabs.GuiChatFilter;
import net.labymod.ingamechat.tools.filter.Filters.Filter;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

@Singleton
@ExclusiveTo(LABY_3)
public class ChatFilterTemplatesLaby3 extends ChatFilterTemplates {

	@EventListener
	public void onGuiOpen(GuiOpenEvent<GuiChatFilter> event) {
		if (event.gui.getClass() != GuiChatFilterWithTemplates.class)
			event.gui = new GuiChatFilterWithTemplates(Reflection.get(event.gui, "defaultInputFieldText"));
	}

	public static Filter createFilter(FilterTemplate t) {
		return new Filter(t.name, t.contains, t.containsNot, false, "note.harp", t.highlighting, t.red, t.green, t.blue, false, !t.highlighting, false, "Global");
	}

}
