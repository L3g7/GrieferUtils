/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Function;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftAction;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftPlayer;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftRecorder;
import dev.l3g7.griefer_utils.features.item.recraft.decompressor.DecompressAction;
import dev.l3g7.griefer_utils.features.item.recraft.decompressor.DecompressPlayer;
import dev.l3g7.griefer_utils.features.item.recraft.decompressor.DecompressRecorder;
import dev.l3g7.griefer_utils.features.item.recraft.recipe.RecipeAction;
import dev.l3g7.griefer_utils.features.item.recraft.recipe.RecipePlayer;
import dev.l3g7.griefer_utils.features.item.recraft.recipe.RecipeRecorder;

import java.util.LinkedList;

import static dev.l3g7.griefer_utils.features.item.recraft.RecraftRecordingCore.RecordingMode.RECIPE;

/**
 * Required because in LabyMod 4 the RecraftRecording has to extend Config
 */
public class RecraftRecordingCore {

	public ActionList actions = new ActionList();
	private final RecraftRecording wrapper;

	final StringSetting name = StringSetting.create()
		.name("Name")
		.description("Wie diese Aufzeichnung heißt.")
		.icon("name_tag");

	final KeySetting key = KeySetting.create()
		.name("Taste")
		.description("Mit welcher Taste diese Aufzeichung abgespielt werden soll.")
		.icon("key")
		.pressCallback(pressed -> {
			if (pressed && ServerCheck.isOnCitybuild() && Recraft.get().isEnabled())
				play(false);
		});

	final SwitchSetting ignoreSubIds = SwitchSetting.create()
		.name("Sub-IDs ignorieren")
		.description("Ob beim Auswählen der Zutaten die Sub-IDs (z.B. unterschiedliche Holz-Typen) ignoriert werden sollen.")
		.icon("carpet_red");

	public final DropDownSetting<RecordingMode> mode = DropDownSetting.create(RecordingMode.class)
		.name("Modus")
		.description("Ob die Aufzeichnung /craft, /rezepte ausführt oder dekomprimiert.")
		.icon("knowledge_book")
		.defaultValue(RECIPE);

	public final SwitchSetting craftAll = SwitchSetting.create()
		.name("Alles vercraften")
		.description("Ob die Aufzeichnung so lange wiederholt werden soll, bis alle Items im Inventar verbraucht wurden.")
		.icon("loop");

	public RecraftRecordingCore(RecraftRecording wrapper) {
		this.wrapper = wrapper;
	}

	public void play(boolean isSuccessor) {
		Recraft.playingSuccessor = isSuccessor;
		Recraft.ignoreSubIds = ignoreSubIds.get();
		mode.get().player.accept(wrapper);
	}

	public void startRecording() {
		mode.get().recorder.accept(wrapper);
	}

	public enum RecordingMode implements Named {

		RECIPE("Rezept", "knowledge_book", RecipeRecorder::startRecording, RecipePlayer::play, RecipeAction::fromJson),
		CRAFT("/craft", "crafting_table", CraftRecorder::startRecording, CraftPlayer::play, CraftAction::fromJson),
		DECOMPRESS("Dekomprimieren", "bundle", DecompressRecorder::startRecording, DecompressPlayer::play, DecompressAction::fromJson);

		private final String displayName;
		public final String icon;
		public final Consumer<RecraftRecording> recorder, player;
		public final Function<JsonElement, RecraftAction> actionParser;

		RecordingMode(String displayName, String icon, Consumer<RecraftRecording> recorder, Consumer<RecraftRecording> player, Function<JsonElement, RecraftAction> actionParser) {
			this.displayName = displayName;
			this.icon = icon;
			this.recorder = recorder;
			this.player = player;
			this.actionParser = actionParser;
		}

		@Override
		public String getName() {
			return displayName;
		}

	}

	private class ActionList extends LinkedList<RecraftAction> {

		@Override
		public boolean add(RecraftAction element) {
			boolean result = super.add(element);
			wrapper.updateStartRecordingIcon();
			return result;
		}

		@Override
		public void clear() {
			super.clear();
			wrapper.updateStartRecordingIcon();
		}

	}

}
