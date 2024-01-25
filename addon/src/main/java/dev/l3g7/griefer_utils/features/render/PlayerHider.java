/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.PlaySoundAtEntityEvent;
import dev.l3g7.griefer_utils.event.events.PlaySoundEvent;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderPlayerEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.player_list_setting.PlayerListSetting;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;

import java.util.Arrays;
import java.util.List;

import static dev.l3g7.griefer_utils.misc.ServerCheck.isOnGrieferGames;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;

@Singleton
public class PlayerHider extends Feature {

	private static final List<String> BLOCKED_SOUNDS = Arrays.asList("random.eat", "random.burp", "random.drink");

	private final BooleanSetting showNPCs = new BooleanSetting()
		.name("NPCs zeigen")
		.icon("steve")
		.description("Ob Spieler, die von GrieferGames erzeugt wurden (z.B. Orbhändler), auch angezeigt werden sollen.")
		.defaultValue(true)
		.callback(() -> {
			if (isOnGrieferGames())
				for (EntityPlayer player : world().playerEntities)
					updatePlayer(player);
		});

	private final PlayerListSetting excludedPlayers = new PlayerListSetting()
		.name("%s. Spieler");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Spieler verstecken")
		.description("Versteckt andere Spieler.")
		.icon("blindness")
		.callback(() -> {
			if (isOnGrieferGames())
				for (EntityPlayer player : world().playerEntities)
					updatePlayer(player);
		})
		.subSettings(showNPCs, new HeaderSetting("Ausgenommene Spieler"), excludedPlayers)
		.addHotkeySetting("das Verstecken von Spielern", null);

	{ excludedPlayers.setContainer(enabled); }

	@EventListener
	public void onTick(TickEvent.ClientTickEvent event) {
		if (world() != null )
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
			Reflection.set(player, hidden, "isImmuneToFire");

		if (hidden) {
			player.setSprinting(false); // hide sprinting particles
			Reflection.invoke(player, "resetPotionEffectMetadata");// hide effect particles
			player.setEating(false); // hide eating particles
			player.clearItemInUse();
		}

		player.setSilent(hidden);
	}

	private boolean showPlayer(Entity player) {
		return player.equals(player()) || excludedPlayers.contains(player.getName(), player.getUniqueID()) || (PlayerUtil.isNPC(player) && showNPCs.get());
	}

}
