/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.recraft;

import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.KeySetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.crafter.CraftPlayer;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.recipe.RecipePlayer;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

/**
 * Original version by Pleezon
 */
@Singleton
public class Recraft extends Feature {

	public static final RecraftRecording tempRecording = new RecraftRecording("Leere Aufzeichnung");

	private final KeySetting key = KeySetting.create()
		.name("Letzten Aufruf wiederholen")
		.description("Wiederholt den letzten \"/rezepte\" oder \"/craft\" Aufruf.")
		.icon(ItemUtil.createItem(Blocks.crafting_table, 0, true))
		.pressCallback(pressed -> {
			if (pressed && ServerCheck.isOnCitybuild() && isEnabled())
				RecipePlayer.play(tempRecording);
		});

	private final RecraftPieMenu pieMenu = new RecraftPieMenu();

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
				pieMenu.open(animation.get(), pages);
				return;
			}

			pieMenu.close();
		});

	public static final RecraftPage.RecraftPageListSetting pages = new RecraftPage.RecraftPageListSetting()
		.name("Seiten")
		.icon(Items.map);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Recraft")
		.description("Wiederholt \"/rezepte\" oder \"/craft\" Aufrufe.\n\nVielen Dank an Pleezon/AntiBannSystem für die Hilfe beim AutoCrafter §c❤")
		.icon(ItemUtil.createItem(Blocks.crafting_table, 0, true))
		.subSettings(key, HeaderSetting.create(), openPieMenu, animation, HeaderSetting.create(), pages);

	public static boolean isPlaying() {
		return RecipePlayer.isPlaying() || CraftPlayer.isPlaying();
	}

}
