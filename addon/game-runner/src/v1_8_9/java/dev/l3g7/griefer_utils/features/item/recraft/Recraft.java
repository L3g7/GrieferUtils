/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftPlayer;
import dev.l3g7.griefer_utils.features.item.recraft.recipe.RecipePlayer;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.features.item.recraft.RecraftBridge.recraftBridge;

@Singleton
public class Recraft extends Feature {

	public static final RecraftRecording tempRecording = recraftBridge.createEmptyRecording();
	public static boolean playingSuccessor;
	public static boolean ignoreSubIds;

	private final SwitchSetting ignoreSubIdsSetting = SwitchSetting.create()
		.name("Sub-IDs ignorieren")
		.description("Ob beim Auswählen der Zutaten die Sub-IDs (z.B. unterschiedliche Holz-Typen) ignoriert werden sollen.")
		.icon("carpet_red")
		.callback(tempRecording.getCore().ignoreSubIds::set);

	private final KeySetting repeatLastRecording = KeySetting.create()
		.name("Letzten Aufruf wiederholen")
		.description("Wiederholt den letzten \"/rezepte\" oder \"/craft\" Aufruf.")
		.icon("crafting_table")
		.subSettings(ignoreSubIdsSetting)
		.pressCallback(pressed -> {
			if (pressed && ServerCheck.isOnCitybuild() && isEnabled())
				tempRecording.getCore().play(false);
		});

	private final SwitchSetting animation = SwitchSetting.create()
		.name("Animation")
		.description("Ob die Öffnen-Animation abgespielt werden soll.")
		.icon("command_menu")
		.defaultValue(true);

	private final KeySetting openPieMenu = KeySetting.create()
		.name("Radialmenü öffnen")
		.icon("key")
		.description("Die Taste, mit der das Radialmenü geöffnet werden soll.")
		.pressCallback(p -> {
			if (mc().currentScreen != null || !isEnabled())
				return;

			if (p) {
				recraftBridge.openPieMenu(animation.get());
				return;
			}

			recraftBridge.closePieMenu();
		});

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Recraft")
		.description("Wiederholt \"/rezepte\" oder \"/craft\" Aufrufe oder dekomprimiert Items.\n\nVielen Dank an Pleezon/AntiBannSystem für die Hilfe beim AutoCrafter §c❤")
		.icon("crafting_table")
		.subSettings(repeatLastRecording, HeaderSetting.create(), openPieMenu, animation, HeaderSetting.create(), recraftBridge.getPagesSetting());

	public static Recraft get() {
		return get(Recraft.class);
	}

	@Override
	public void init() {
		super.init();
		recraftBridge.init();
	}

	public static boolean isPlaying() {
		return RecipePlayer.isPlaying() || CraftPlayer.isPlaying();
	}

}
