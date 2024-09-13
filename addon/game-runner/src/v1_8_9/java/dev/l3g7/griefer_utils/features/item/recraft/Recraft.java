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
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftPlayer;
import dev.l3g7.griefer_utils.features.item.recraft.recipe.RecipePlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

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
		.icon(new ItemStack(Blocks.log, 1, 2))
		.callback(tempRecording.getCore().ignoreSubIds::set);

	private final KeySetting repeatLastRecording = KeySetting.create()
		.name("Letzten Aufruf wiederholen")
		.description("Wiederholt den letzten \"/rezepte\" oder \"/craft\" Aufruf.")
		.icon(ItemUtil.createItem(Blocks.crafting_table, 0, true))
		.subSettings(ignoreSubIdsSetting)
		.pressCallback(pressed -> {
			if (pressed && ServerCheck.isOnCitybuild() && isEnabled())
				tempRecording.getCore().play(false);
		});

	private final SwitchSetting animation = SwitchSetting.create()
		.name("Animation")
		.description("Ob die Öffnen-Animation abgespielt werden soll.")
		.icon("command_pie_menu")
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
		.icon(ItemUtil.createItem(Blocks.crafting_table, 0, true))
		.subSettings(repeatLastRecording, HeaderSetting.create(), openPieMenu, animation, HeaderSetting.create(), recraftBridge.getPagesSetting());

	@Override
	public void init() {
		super.init();
		recraftBridge.init();
	}

	public static boolean isPlaying() {
		return RecipePlayer.isPlaying() || CraftPlayer.isPlaying();
	}

}
