/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.clean_up_player_menu;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class CleanUpPlayerMenu extends Feature {

	@MainElement
	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Spielermenü aufräumen")
		.description("Entfernt Spielermenü-Einträge, die eigentlich nicht entfernt werden können.")
		.icon("player_menu");

	public static CleanUpPlayerMenu get() {
		return get(CleanUpPlayerMenu.class);
	}

	public static abstract class CleanUpPlayerMenuBridge<V> {

		private List<V> shownEntries;
		private List<V> allEntries;
		private String statesKey;
		private int mask = 0;

		protected abstract List<V> getEntriesReference();

		protected abstract String getName(V entry);

		public void init() {
			shownEntries = getEntriesReference();
			allEntries = new ArrayList<>(shownEntries);

			statesKey = CleanUpPlayerMenu.get().getConfigKey() + ".entries";
			if (Config.has(statesKey)) {
				mask = Config.get(statesKey).getAsInt();
				updateEntries();
			}

			List<SwitchSetting> settings = new ArrayList<>();

			for (int i = 0; i < allEntries.size(); i++) {
				String name = getName(allEntries.get(i));
				int index = 1 << i;

				settings.add(SwitchSetting.create()
					.name(name)
					.description("Ob der Spielermenü-Eintrag \"" + name + "\" angezeigt werden soll.")
					.icon("player_menu")
					.defaultValue((mask & index) != 0)
					.callback(b -> {
						if (b)
							mask |= index;
						else
							mask &= ~index;

						updateEntries();
					}));
			}

			enabled.subSettings(settings.toArray(new SwitchSetting[0]));
			enabled.callback(this::updateEntries);
			CleanUpPlayerMenu.get().getCategory().callback(this::updateEntries);
		}

		private void updateEntries() {
			shownEntries.clear();
			boolean enabled = CleanUpPlayerMenu.get().isEnabled();

			for (int i = 0; i < allEntries.size(); i++)
				if ((mask & 1 << i) != 0 || !enabled)
					shownEntries.add(allEntries.get(i));

			Config.set(statesKey, new JsonPrimitive(mask));
			Config.save();
		}

	}

}
