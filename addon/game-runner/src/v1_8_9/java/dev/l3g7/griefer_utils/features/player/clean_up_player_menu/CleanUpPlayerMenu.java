/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.clean_up_player_menu;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.interaction.BulletPoint;
import net.labymod.api.util.KeyValue;
import net.labymod.main.LabyMod;
import net.labymod.user.util.UserActionEntry;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

/**
 * Removes predefined player menu entries.
 */
@Singleton
public class CleanUpPlayerMenu extends Feature {

	@MainElement
	private static final SwitchSetting enabled = SwitchSetting.create()
		.name("Spielermenü aufräumen")
		.description("Entfernt Spielermenü-Einträge, die eigentlich nicht entfernt werden können.")
		.icon("player_menu");

	private static CleanUpPlayerMenu get() {
		return get(CleanUpPlayerMenu.class);
	}

	private static class PlayerMenuCleaner<V> {

		private final List<V> shownEntriesRef;
		private final List<V> allEntries;
		private final String statesKey;
		private int mask = 0; // Remove everything by default

		public PlayerMenuCleaner(List<V> entriesReference) {
			shownEntriesRef = entriesReference;
			allEntries = new ArrayList<>(entriesReference);

			statesKey = CleanUpPlayerMenu.get().getConfigKey() + ".entries";
			if (Config.has(statesKey)) {
				mask = Config.get(statesKey).getAsInt();
				updateEntriesRef();
			}
		}

		public void register(Function<V, String> nameGetter) {
			List<SwitchSetting> settings = new ArrayList<>();

			for (int i = 0; i < allEntries.size(); i++) {
				String name = nameGetter.apply(allEntries.get(i));
				int index = 1 << i;

				SwitchSetting setting = SwitchSetting.create()
					.name(name)
					.description("Ob der Spielermenü-Eintrag \"" + name + "\" angezeigt werden soll.")
					.icon("player_menu")
					.defaultValue((mask & index) != 0)
					.callback(b -> {
						if (b)
							mask |= index;
						else
							mask &= ~index;

						updateEntriesRef();
					});

				setting.create(enabled);
				settings.add(setting);
			}

			enabled.subSettings(settings.toArray(new SwitchSetting[0]));
			enabled.callback(this::updateEntriesRef);
			CleanUpPlayerMenu.get().getCategory().callback(this::updateEntriesRef);
		}

		private void updateEntriesRef() {
			shownEntriesRef.clear();
			boolean enabled = CleanUpPlayerMenu.get().isEnabled();

			for (int i = 0; i < allEntries.size(); i++)
				if ((mask & 1 << i) != 0 || !enabled)
					shownEntriesRef.add(allEntries.get(i));

			Config.set(statesKey, new JsonPrimitive(mask));
			Config.save();
		}

	}

	@ExclusiveTo(LABY_3)
	private static class Laby3 {

		@OnStartupComplete
		public static void init() {
			List<UserActionEntry> entriesRef = Reflection.get(LabyMod.getInstance().getUserManager().getUserActionGui(), "defaultEntries");
			new PlayerMenuCleaner<>(entriesRef).register(UserActionEntry::getDisplayName);
		}

	}

	@ExclusiveTo(LABY_4)
	private static class Laby4 {

		@OnStartupComplete
		public static void init() {
			List<KeyValue<BulletPoint>> entriesRef = Laby.references().interactionMenuRegistry().getElements();
			new PlayerMenuCleaner<>(entriesRef).register(e ->
				((IChatComponent) e.getValue().getTitle()).getUnformattedText());
		}

	}

}
