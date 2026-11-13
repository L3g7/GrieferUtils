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
import dev.l3g7.griefer_utils.core.api.util.StringUtil;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.interaction.BulletPoint;
import net.labymod.api.util.KeyValue;
import net.labymod.main.LabyMod;
import net.labymod.user.util.UserActionEntry;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

	private static class PlayerMenuEntry {
		private final SwitchSetting shouldHide;

		public PlayerMenuEntry(String name, String partialConfigKey, PlayerMenuCleaner<?> cleaner) {
			String configKey = CleanUpPlayerMenu.get().getConfigKey() + ".entries." + partialConfigKey;

			boolean isHidden = true;
			if (Config.has(configKey))
				isHidden = Config.get(configKey).getAsBoolean();

			shouldHide = SwitchSetting.create()
				.name(name)
				.description("Ob der Spielermenü-Eintrag \"" + name + "\" versteckt werden soll.")
				.icon("player_menu")
				.defaultValue(isHidden)
				.callback(b -> {
					Config.set(configKey, new JsonPrimitive(b));
					Config.save();

					cleaner.updateShownEntries();
				});
		}
	}

	private static class PlayerMenuCleaner<V> {

		private final List<V> shownEntriesRef;
		private final List<V> allEntries;
		private final Map<V, PlayerMenuEntry> entries = new LinkedHashMap<>();

		public PlayerMenuCleaner(List<V> entriesReference) {
			shownEntriesRef = entriesReference;
			allEntries = new ArrayList<>(entriesReference);
		}

		public void register(V target, String name, String partialConfigKey) {
			entries.put(target, new PlayerMenuEntry(name, partialConfigKey, this));
		}

		public void init() {
			List<SwitchSetting> settings = new ArrayList<>();

			for (Entry<V, PlayerMenuEntry> entry : entries.entrySet()) {
				SwitchSetting setting = entry.getValue().shouldHide;
				setting.create(enabled);
				settings.add(setting);
			}

			enabled.subSettings(settings.toArray(new SwitchSetting[0]));
			enabled.callback(this::updateShownEntries);
			CleanUpPlayerMenu.get().getCategory().callback(this::updateShownEntries);

			updateShownEntries();
		}

		private void updateShownEntries() {
			shownEntriesRef.clear();
			boolean enabled = CleanUpPlayerMenu.get().isEnabled();

			for (V allEntry : allEntries) {
				if (entries.containsKey(allEntry)) {
					var entry = entries.get(allEntry);
					if (!(enabled && entry.shouldHide.get()))
						shownEntriesRef.add(allEntry);
				}
			}
		}

	}

	@ExclusiveTo(LABY_3)
	private static class Laby3 {

		@OnStartupComplete
		public static void init() {
			String[] configKeys = new String[]{"laby_net", "copy_name", "name_history", "report_cloak", "add_friend"};
			List<UserActionEntry> entriesRef = Reflection.get(LabyMod.getInstance().getUserManager().getUserActionGui(), "defaultEntries");
			PlayerMenuCleaner<UserActionEntry> cleaner = new PlayerMenuCleaner<>(entriesRef);

			for (int i = 0; i < entriesRef.size(); i++) {
				var entry = entriesRef.get(i);
				cleaner.register(entry, entry.getDisplayName(), configKeys[i]);
			}

			cleaner.init();
		}

	}

	@ExclusiveTo(LABY_4)
	private static class Laby4 {

		@OnStartupComplete
		public static void init() {
			List<KeyValue<BulletPoint>> entriesRef = Laby.references().interactionMenuRegistry().getElements();
			PlayerMenuCleaner<KeyValue<BulletPoint>> cleaner = new PlayerMenuCleaner<>(entriesRef);

			for (KeyValue<BulletPoint> kv : entriesRef) {
				if (kv.getValue().getTitle() instanceof ChatComponentTranslation title) {
					cleaner.register(kv,
						((IChatComponent) title).getUnformattedText(),
						StringUtil.convertCasing(kv.getValue().getClass().getSimpleName().replaceFirst("BulletPoint$", ""))
					);
				}
				// Ignore bullet points with fixed name, as they are either AutoText- or ServerBulletPoints
			}

			cleaner.init();
		}

	}

}
