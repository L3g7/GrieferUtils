/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.player;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import net.labymod.main.LabyMod;
import net.labymod.user.util.UserActionEntry;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_3;

@Singleton
@ExclusiveTo(LABY_3)
public class CleanUpPlayerMenuL3 extends Feature {

	private List<UserActionEntry> defaultEntries;
	private List<UserActionEntry> allDefaultEntries;
	private String statesKey;
	private int shownEntries;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spielermenü aufräumen")
		.description("Entfernt Spielermenü-Einträge, die eigentlich nicht entfernt werden können.")
		.icon("labymod_3/playermenu");

	@Override
	public void init() {
		super.init();
		defaultEntries = Reflection.get(LabyMod.getInstance().getUserManager().getUserActionGui(), "defaultEntries");
		allDefaultEntries = new ArrayList<>(defaultEntries);
		shownEntries = 0;

		statesKey = getConfigKey() + ".entries";
		if (Config.has(statesKey)) {
			shownEntries = Config.get(statesKey).getAsInt();
			updateEntries();
		}

		List<SwitchSetting> settings = new ArrayList<>();

		for (int i = 0; i < allDefaultEntries.size(); i++) {
			UserActionEntry entry = allDefaultEntries.get(i);
			int index = 1 << i;

			settings.add(SwitchSetting.create()
				.name(entry.getDisplayName())
				.description("Ob der Spielermenü-Eintrag \"" + entry.getDisplayName() + "\" angezeigt werden soll.")
				.icon("labymod_3/playermenu")
				.defaultValue((shownEntries & index) != 0)
				.callback(b -> {
					if (b)
						shownEntries |= index;
					else
						shownEntries &= ~index;

					updateEntries();
				}));
		}

		enabled.subSettings(settings.toArray(new SwitchSetting[0]));
	}

	private void updateEntries() {
		defaultEntries.clear();

		for (int i = 0; i < allDefaultEntries.size(); i++)
			if ((shownEntries & 1 << i) != 0)
				defaultEntries.add(allDefaultEntries.get(i));

		Config.set(statesKey, new JsonPrimitive(shownEntries));
		Config.save();
	}

}
