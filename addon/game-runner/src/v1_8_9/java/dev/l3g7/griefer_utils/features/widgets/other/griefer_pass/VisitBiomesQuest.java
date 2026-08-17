/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.other.griefer_pass;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.HashSet;
import java.util.Set;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

public class VisitBiomesQuest extends AbstractQuest {

	Set<Byte> visitedBiomes = new HashSet<>();

	@EventListener
	private void onTick(TickEvent.ClientTickEvent event) {
		EntityPlayerSP player = player();
		WorldClient world = world();
		if (player == null || world == null)
			return;

		BlockPos playerPos = new BlockPos(player.posX, player.posY, player.posZ);
		Chunk chunk = world.getChunkFromBlockCoords(playerPos);
		int x = playerPos.getX() & 15;
		int z = playerPos.getZ() & 15;
		byte biome = chunk.getBiomeArray()[z << 4 | x];
		if (visitedBiomes.add(biome))
			increaseAmount();
	}

	@Override
	public JsonObject serialize() {
		JsonObject obj = super.serialize();
		JsonArray array = new JsonArray();
		for (Byte visitedBiome : visitedBiomes)
			array.add(new JsonPrimitive(visitedBiome));
		obj.add("biomes", array);
		return obj;
	}

	public void deserializeBiomes(JsonObject obj) {
		JsonArray array = obj.getAsJsonArray("biomes");
		for (JsonElement element : array)
			visitedBiomes.add(element.getAsByte());
	}

}
