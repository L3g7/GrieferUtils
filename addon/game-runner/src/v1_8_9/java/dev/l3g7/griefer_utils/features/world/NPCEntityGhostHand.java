/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.MouseClickEvent;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.ArrayList;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

/**
 * Displays a message when players join or leave.
 */
@Singleton
public class NPCEntityGhostHand extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("NPC-Klick-Helfer")
		.description("Ermöglicht das Klicken auf NPCs durch Entities.")
		.icon("left_click");

	@EventListener
	public void onClick(MouseClickEvent.RightClickEvent event) {
		if (world() == null || player() == null)
			return;

		// Don't intercept if targeted entity is a NPC
		if (PlayerUtil.isNPC(mc().pointedEntity))
			return;

		float reachDistance = mc().playerController.getBlockReachDistance();

		Vec3 eyes = player().getPositionEyes(partialTicks());
		Vec3 entityHitPos = null;
		Entity hitEntity = null;

		for (Entity entity : new ArrayList<>(world().loadedEntityList)) {
			if (!(entity instanceof EntityOtherPlayerMP))
				continue;

			// Skip if entity isn't a NPC
			if (!PlayerUtil.isNPC(entity))
				continue;

			// Check if entity is hit
			Vec3 lookVec = player().getLook(partialTicks());
			Vec3 maxTracePos = eyes.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

			float collisionSize = entity.getCollisionBorderSize();
			AxisAlignedBB hitBox = entity.getEntityBoundingBox().expand(collisionSize, collisionSize, collisionSize);

			MovingObjectPosition intercept = hitBox.calculateIntercept(eyes, maxTracePos);
			if (intercept != null) {
				entityHitPos = intercept.hitVec;
				hitEntity = entity;
				break;
			}
		}

		if (hitEntity == null)
			return;

		// Check if a block is in the way
		MovingObjectPosition targetedBlock = mc().getRenderViewEntity().rayTrace(reachDistance, partialTicks());
		if (targetedBlock != null && targetedBlock.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
			if (eyes.squareDistanceTo(targetedBlock.hitVec) < eyes.squareDistanceTo(entityHitPos)) {
				// The block is in front of the npc
				return;
			}
		}

		mc().playerController.interactWithEntitySendPacket(player(), hitEntity);
	}

}
