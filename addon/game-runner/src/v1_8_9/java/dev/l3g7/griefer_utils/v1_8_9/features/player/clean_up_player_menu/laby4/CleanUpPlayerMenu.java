/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.player.clean_up_player_menu.laby4;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.interaction.BulletPoint;
import net.labymod.api.util.KeyValue;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;

@Singleton
@ExclusiveTo(LABY_4)
public class CleanUpPlayerMenu extends Feature {

	private List<KeyValue<BulletPoint>> entries;
	private List<KeyValue<BulletPoint>> allEntries;
	private String statesKey;
	private int shownEntries = 0;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spielermenü aufräumen")
		.description("Entfernt Spielermenü-Einträge, die eigentlich nicht entfernt werden können.")
		.icon("labymod_3/playermenu");

	@Override
	public void init() {
		super.init();
		entries = Laby.references().interactionMenuRegistry().getElements();
		allEntries = new ArrayList<>(entries);

		statesKey = getConfigKey() + ".entries";
		if (Config.has(statesKey)) {
			shownEntries = Config.get(statesKey).getAsInt();
			updateEntries();
		}

		List<SwitchSetting> settings = new ArrayList<>();

		for (int i = 0; i < allEntries.size(); i++) {
			IChatComponent title = (IChatComponent) allEntries.get(i).getValue().getTitle();
			String name = title.getUnformattedText();
			int index = 1 << i;

			settings.add(SwitchSetting.create()
				.name(name)
				.description("Ob der Spielermenü-Eintrag \"" + name + "\" angezeigt werden soll.")
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
		entries.clear();

		for (int i = 0; i < allEntries.size(); i++)
			if ((shownEntries & 1 << i) != 0)
				entries.add(allEntries.get(i));

		Config.set(statesKey, new JsonPrimitive(shownEntries));
		Config.save();
	}

}
