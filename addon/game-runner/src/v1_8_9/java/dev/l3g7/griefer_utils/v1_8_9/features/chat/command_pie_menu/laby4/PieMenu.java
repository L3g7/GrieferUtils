/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat.command_pie_menu.laby4;

import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

public class PieMenu extends dev.l3g7.griefer_utils.v1_8_9.misc.gui.guis.PieMenu {

	public void open(boolean animation, PageListSetting pages) { // NOTE: refactor (merge to CommandPieMenu?)
		List<Pair<String, List<Pair<String, Runnable>>>> allPages = new ArrayList<>();

		for (Page page : pages.get()) {
			List<Pair<String, Runnable>> entries = new ArrayList<>();

			for (Page.Entry entry : page.entries()) {
				if (!entry.citybuild().isOnCb())
					continue;

				entries.add(Pair.of(entry.name().replace('&', '§'), () -> {
					if (!MessageEvent.MessageSendEvent.post(entry.command()))
						player().sendChatMessage(entry.command());
				}));
			}

			allPages.add(Pair.of(page.name().replace('&', '§'), entries));
		}

		open(animation, allPages);
	}

}