/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.PlaySoundAtEntityEvent;
import dev.l3g7.griefer_utils.core.events.PlaySoundEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderPlayerEvent;
import dev.l3g7.griefer_utils.core.settings.player_list.PlayerListEntry;
import dev.l3g7.griefer_utils.core.settings.player_list.PlayerListSettingLaby3;
import dev.l3g7.griefer_utils.core.settings.player_list.PlayerListSettingLaby4;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.misc.ServerCheck.isOnGrieferGames;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

@Singleton
public class PlayerHider extends Feature {

	private static final List<String> BLOCKED_SOUNDS = Arrays.asList("random.eat", "random.burp", "random.drink");

	private final SwitchSetting showNPCs = SwitchSetting.create()
		.name("NPCs zeigen")
		.icon("steve")
		.description("Ob Spieler, die von GrieferGames erzeugt wurden (z.B. Orbhändler), auch angezeigt werden sollen.")
		.defaultValue(true)
		.callback(() -> {
			if (isOnGrieferGames())
				for (EntityPlayer player : world().playerEntities)
					updatePlayer(player);
		});

	private final AbstractSetting<?, List<PlayerListEntry>> excludedPlayers = temp();

	private static AbstractSetting<?, List<PlayerListEntry>> temp() { // TODO refactor
		if (LABY_4.isActive())
			return new PlayerListSettingLaby4()
				.name("Ausgenommene Spieler")
				.icon("light_bulb");
		else
			return new PlayerListSettingLaby3()
				.name("Ausgenommene Spieler")
				.icon("light_bulb");
	}

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spieler verstecken")
		.description("Versteckt andere Spieler.")
		.icon("blindness")
		.callback(() -> {
			if (isOnGrieferGames())
				for (EntityPlayer player : world().playerEntities)
					updatePlayer(player);
		})
		.subSettings(showNPCs, excludedPlayers)
		.addHotkeySetting("das Verstecken von Spielern", null);

	@EventListener
	public void onTick(TickEvent.ClientTickEvent event) {
		if (world() != null)
			for (EntityPlayer player : world().playerEntities)
				updatePlayer(player);
	}

	/**
	 * Handles the player model
	 */
	@EventListener
	public void onEntityRender(RenderPlayerEvent event) {
		if (!showPlayer(((event.player))))
			event.cancel();
	}

	private boolean playSound = false;

	/**
	 * Makes sure your own sounds are still played
	 */
	@EventListener
	public void onSoundPlayAtEntity(PlaySoundAtEntityEvent event) {
		if (showPlayer(event.entity))
			playSound = true;
	}

	/**
	 * Cancels other players' sounds
	 */
	@EventListener
	public void onSoundPlay(PlaySoundEvent event) {
		if (playSound) {
			playSound = false;
			return;
		}

		if (event.name.startsWith("step.") || BLOCKED_SOUNDS.contains(event.name))
			event.cancel();
	}

	private void updatePlayer(EntityPlayer player) {
		boolean hidden = isEnabled() && !showPlayer(player);

		// Shadows
		if (player.isInvisible() != hidden)
			player.setInvisible(hidden || player.isPotionActive(Potion.invisibility));

		// Fire
		if (player.isImmuneToFire() != hidden)
			Reflection.set(player, "isImmuneToFire", hidden);

		if (hidden) {
			player.setSprinting(false); // hide sprinting particles
			Reflection.invoke(player, "resetPotionEffectMetadata");// hide effect particles
			player.setEating(false); // hide eating particles
			player.clearItemInUse();
		}

		player.setSilent(hidden);
	}

	private boolean showPlayer(Entity player) {
		if (player.equals(player()) || (PlayerUtil.isNPC(player) && showNPCs.get()))
			return true;

		String name = player.getName();
		UUID uuid = player.getUniqueID();
		if (name == null && uuid == null)
			return false;

		for (PlayerListEntry entry : excludedPlayers.get())
			if (name == null ? uuid.toString().equalsIgnoreCase(entry.id) : name.equalsIgnoreCase(entry.name))
				return true;

		return false;
	}

}
