/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;
import dev.l3g7.griefer_utils.settings.elements.player_list_setting.PlayerListSetting;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;
import static dev.l3g7.griefer_utils.misc.ServerCheck.isOnGrieferGames;

@Singleton
public class PlayerHider extends Feature {

	private static final List<String> BLOCKED_SOUNDS = Arrays.asList("random.eat", "random.burp", "random.drink");

	private final KeySetting key = new KeySetting()
		.name("Taste")
		.icon("key")
		.pressCallback(pressed -> {
			if (pressed) {
				BooleanSetting enabled = ((BooleanSetting) getMainElement());
				enabled.set(!enabled.get());
			}
		});

	private final BooleanSetting showNPCs = new BooleanSetting()
		.name("NPCs zeigen")
		.icon("steve")
		.defaultValue(true)
		.callback(isActive -> {
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
		.callback(isActive -> {
			if (isOnGrieferGames())
				for (EntityPlayer player : world().playerEntities)
					updatePlayer(player);
		})
		.subSettings(key, showNPCs, new HeaderSetting("Ausgenommene Spieler"), excludedPlayers);

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
	public void onEntityRender(RenderPlayerEvent.Pre event) {
		event.setCanceled(!showPlayer(event.entity));
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
			event.result = null;
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
