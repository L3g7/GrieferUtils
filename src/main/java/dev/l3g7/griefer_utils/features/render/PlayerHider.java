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

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;
import static dev.l3g7.griefer_utils.util.misc.ServerCheck.isOnGrieferGames;

@Singleton
public class PlayerHider extends Feature {

	private static final List<String> BLOCKED_SOUNDS = Arrays.asList("random.eat", "random.burp", "random.drink");

	private boolean playingOwnSounds = false;

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
		});

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!isOnGrieferGames())
			return;

		for (EntityPlayer player : world().playerEntities)
			updatePlayer(player);
	}

	/**
	 * Handles the player model
	 */
	@SubscribeEvent
	public void onEntityRender(RenderPlayerEvent.Pre event) {
		if (!isOnGrieferGames() || event.entity == player())
			return;

		event.setCanceled(true);
	}

	/**
	 * Makes sure your own sounds are still played
	 */
	@SubscribeEvent
	public void onSoundPlayAtEntity(PlaySoundAtEntityEvent event) {
		if (event.entity.equals(player()))
			playingOwnSounds = true;
	}

	/**
	 * Cancels other players' sounds
	 */
	@SubscribeEvent
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
		if (player == player())
			return;

		// Shadows
		if (player.isInvisible() != isEnabled())
			player.setInvisible(isEnabled() || player.isPotionActive(Potion.invisibility));

		// Fire
		if (player.isImmuneToFire() != isEnabled())
			Reflection.set(player, isEnabled(), "isImmuneToFire");

		// Sprinting particles
		player.setSprinting(!isEnabled());

		// Effect particles
		if (isEnabled())
			Reflection.invoke(player, "resetPotionEffectMetadata");
		else
			Reflection.invoke(player, "updatePotionMetadata");

		player.setSilent(isEnabled());
		player.setEating(false);
		player.clearItemInUse();
	}

}