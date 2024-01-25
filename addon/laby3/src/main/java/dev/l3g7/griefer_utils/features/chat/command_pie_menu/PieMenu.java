/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.command_pie_menu;

import dev.l3g7.griefer_utils.core.misc.functions.Runnable;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.ModColor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

public class PieMenu extends dev.l3g7.griefer_utils.misc.PieMenu {

	public void open(boolean animation, SettingsElement entryContainer) {
		List<Pair<String, List<Pair<String, Runnable>>>> allPages = new ArrayList<>();

		for (SettingsElement pageElement : entryContainer.getSubSettings().getElements()) {
			if (!(pageElement instanceof PieMenuPageSetting))
				continue;

			List<Pair<String, Runnable>> entries = new ArrayList<>();

			for (SettingsElement element : pageElement.getSubSettings().getElements()) {
				if (!(element instanceof PieMenuEntrySetting))
					continue;

				PieMenuEntrySetting entry = (PieMenuEntrySetting) element;
				String cb = entry.citybuild.get().getDisplayName();
				String srv = MinecraftUtil.getServerFromScoreboard();
				if (!cb.equals("Egal") && !srv.equals(cb))
					continue;

				entries.add(Pair.of(ModColor.createColors(entry.name.get()), () -> {
					if (!MessageEvent.MessageSendEvent.post(entry.command.get()))
						player().sendChatMessage(entry.command.get());
				}));
			}

			if (!entries.isEmpty())
				allPages.add(Pair.of(ModColor.createColors(((PieMenuPageSetting) pageElement).name.get()), entries));
		}

		open(animation, allPages);
	}

}