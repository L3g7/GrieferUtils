/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.PlayerListSetting;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;
import static dev.l3g7.griefer_utils.util.misc.ServerCheck.isOnGrieferGames;

@Singleton
public class PlayerHider extends Feature {

	private static final List<String> BLOCKED_SOUNDS = Arrays.asList("random.eat", "random.burp", "random.drink");

	private boolean playingOwnSounds = false;

	private final PlayerListSetting excludedPlayers = new PlayerListSetting()
		.name("%s. Spieler")
		.config("tweaks.player_hider.excluded_players");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Spieler verstecken")
		.description("Versteckt andere Spieler.")
		.icon("blindness")
		.defaultValue(false)
		.callback(isActive -> {
			if (isOnGrieferGames())
				for (EntityPlayer player : world().playerEntities)
					updatePlayer(player);
		})
		.subSettings(new HeaderSetting("Ausgenommene Spieler"), excludedPlayers);

	{ excludedPlayers.setContainer(enabled); }

	private final List<UUID> shownPlayers = new ArrayList<>();

	@EventListener
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!isOnGrieferGames())
			return;

		for (EntityPlayer player : world().playerEntities)
			updatePlayer(player);
	}

	/**
	 * Handles the player model
	 */
	@EventListener
	public void onEntityRender(RenderPlayerEvent.Pre event) {
		if (!isOnGrieferGames() || showPlayer(event.entity))
			return;

		event.setCanceled(true);
	}

	/**
	 * Makes sure your own sounds are still played
	 */
	@EventListener
	public void onSoundPlayAtEntity(PlaySoundAtEntityEvent event) {
		if (showPlayer(event.entity))
			playingOwnSounds = true;
	}

	/**
	 * Cancels other players' sounds
	 */
	@EventListener
	public void onSoundPlay(PlaySoundEvent event) {
		if (!isOnGrieferGames())
			return;

		if (playingOwnSounds) {
			playingOwnSounds = false;
			return;
		}

		if (event.name.startsWith("step.") || BLOCKED_SOUNDS.contains(event.name))
			event.result = null;
	}

	private void updatePlayer(EntityPlayer player) {
		if (player.equals(player()) || shownPlayers.contains(player.getUniqueID()))
			return;

		boolean hide = isEnabled();

		// Ensure player is shown when it was added while PlayerHider is active
		if (excludedPlayers.get().contains(player.getUniqueID())) {
			shownPlayers.add(player.getUniqueID());
			hide = false;
		}

		// Shadows
		if (player.isInvisible() != hide)
			player.setInvisible(hide || player.isPotionActive(Potion.invisibility));

		// Fire
		if (player.isImmuneToFire() != hide)
			Reflection.set(player, hide, "isImmuneToFire");

		// Sprinting particles
		if (hide)
			player.setSprinting(false);

		// Effect particles
		if (hide)
			Reflection.invoke(player, "resetPotionEffectMetadata");
		else
			Reflection.invoke(player, "updatePotionMetadata");

		player.setSilent(hide);
		player.setEating(false);
		player.clearItemInUse();
	}

	private boolean showPlayer(Entity player) {
		return player.equals(player()) || excludedPlayers.get().contains(player.getUniqueID());
	}

}