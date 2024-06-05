/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules.laby4.spawn_counter;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static net.minecraft.util.EnumFacing.*;

@ExclusiveTo(LABY_4)
class RoundHandler {

	private static final EnumFacing[] HORIZONTALS = new EnumFacing[] {SOUTH, WEST, NORTH, EAST};
	private static final List<String> excludedCitybuilds = ImmutableList.of("Nature", "Extreme", "Lava", "Wasser", "CBE");
	private static final byte[] quadrantData = new byte[] {
		-1, -1,
		-1, +1,
		+1, -1,
		+1, +1
	};

	private final SpawnCounter spawnCounter;

	private World spawnWorld;
	private AxisAlignedBB spawnBox;
	private BlockPos spawnMiddle;
	private byte startQuadrant = -1;
	private byte visitedQuadrants = 0;

	int roundsFlown;
	int roundsRan;

	private boolean hasFlown;
	private boolean accountForStartBonus;
	private long startTime = 0;

	public RoundHandler(SpawnCounter spawnCounter) {
		this.spawnCounter = spawnCounter;
		EventRegisterer.register(this);
	}

	@EventListener
	private void onTick(TickEvent.ClientTickEvent event) {
		if (player() == null || spawnWorld == null)
			return;

		boolean isInSpawnBox = player().posX > spawnBox.minX && player().posX < spawnBox.maxX
			&& player().posZ > spawnBox.minZ && player().posZ < spawnBox.maxZ;

		// Rough check if the player is at spawn (and not in another world)
		if (isInSpawnBox || world() != spawnWorld) {
			visitedQuadrants = 0;
			startQuadrant = -1;
			return;
		}

		if (player().capabilities.isFlying)
			hasFlown = true;

		BlockPos playerPos = player().getPosition().subtract(spawnMiddle);

		for (byte i = 0; i < 4; i++) {
			byte x = quadrantData[i * 2];
			byte z = quadrantData[i * 2 + 1];

			if (Math.signum(playerPos.getX()) != x || Math.signum(playerPos.getZ()) != z)
				continue;

			if (startQuadrant == -1) {
				startQuadrant = i;
				accountForStartBonus = true;
				startTime = System.currentTimeMillis();
			}

			visitedQuadrants |= 1 << i;

			// Check if player has completed a round
			if (visitedQuadrants != 15 || startQuadrant != i)
				return;

			visitedQuadrants = 0;
			long delta = System.currentTimeMillis() - startTime;
			startTime = System.currentTimeMillis();

			if (accountForStartBonus) {
				delta *= 1.25d;
				accountForStartBonus = false;
			}

			spawnCounter.leaderboardHandler.onRoundComplete(hasFlown);

			if (hasFlown) {
				roundsFlown++;
				spawnCounter.notificationType.get().notifier.accept(String.format("§fDu bist deine §e%dte§f Runde in §e%.1f§f Sekunden abgeflogen!", roundsFlown, Math.round(delta / 100d) / 10d));
				Config.set(SpawnCounter.configKey + "flown", new JsonPrimitive(roundsFlown));
				hasFlown = false;
			} else {
				roundsRan++;
				spawnCounter.notificationType.get().notifier.accept(String.format("§fDu bist deine §e%dte§f Runde in §e%.1f§f Sekunden abgelaufen!", roundsRan, Math.round(delta / 100d) / 10d));
				Config.set(SpawnCounter.configKey + "ran", new JsonPrimitive(roundsRan));
			}

			Config.save();
		}
	}

	@EventListener
	private void onPlayerTeleport(PacketReceiveEvent<S08PacketPlayerPosLook> event) {
		visitedQuadrants = 0;
		startQuadrant = -1;
	}

	@EventListener
	private void onMessageReceive(MessageReceiveEvent event) {
		if (!ServerCheck.isOnGrieferGames() || player() == null)
			return;

		if (!event.message.getFormattedText().equals("§r§2§l[Switcher] §r§eLade Daten herunter!§r"))
			return;

		visitedQuadrants = 0;
		startQuadrant = -1;
		spawnWorld = null;
		determineSpawn(player().getPosition().down());
	}

	private void determineSpawn(BlockPos pos) {
		if (excludedCitybuilds.contains(getServerFromScoreboard()))
			return;

		for (EnumFacing f : HORIZONTALS) {
			BlockPos a = pos.offset(f);
			BlockPos b = pos.offset(f.rotateY());
			if (!isSpawnMiddle(a) || !isSpawnMiddle(b))
				continue;

			spawnMiddle = new BlockPos(Math.max(a.getX(), b.getX()), pos.getY(), Math.max(a.getZ(), b.getZ()));

			spawnBox = new AxisAlignedBB(spawnMiddle, spawnMiddle).expand(18, 0, 18);
			spawnWorld = world();

			return;
		}
	}

	private boolean isSpawnMiddle(BlockPos pos) {
		Block targetBlock = getServerFromScoreboard().equals("Event") ? Blocks.quartz_block : Blocks.stonebrick;
		return world().getBlockState(pos).getBlock() == targetBlock;
	}

}
