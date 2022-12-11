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

package dev.l3g7.griefer_utils.features.world;

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Timer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

/**
 * Displays a message when players join or leave.
 */
@Singleton
public class NPCEntityGhostHand extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("NPC-Klick-Helfer")
		.description("Ermöglicht das Klicken auf NPCs durch Entities.")
		.icon("cursor");

	@EventListener
	public void onClick(MouseEvent event) {
		if (world() == null)
			return;

		// Only intercept right clicks
		if (event.isCanceled() || !event.buttonstate || event.button != 1)
			return;

		// Don't intercept if targeted entity is a NPC
		if (isNPC(mc().pointedEntity))
			return;

		float reachDistance = mc().playerController.getBlockReachDistance();

		Timer timer = Reflection.get(mc(), "timer");
		float partialTicks = timer.renderPartialTicks;
		Vec3 eyes = player().getPositionEyes(partialTicks);

		// Check if block is in the way (Don't want to create a cheat xD)
		MovingObjectPosition targetedBlock = mc().getRenderViewEntity().rayTrace(reachDistance, partialTicks);
		if (targetedBlock != null && targetedBlock.typeOfHit != MovingObjectPosition.MovingObjectType.MISS)
			return;

		for (Entity entity : world().loadedEntityList) {
			if (!(entity instanceof EntityOtherPlayerMP))
				continue;

			// Skip if entity isn't a NPC
			if (!isNPC(entity))
				continue;

			// Check if entity is hit
			Vec3 lookVec = player().getLook(partialTicks);
			Vec3 maxTracePos = eyes.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

			float collisionSize = entity.getCollisionBorderSize();
			AxisAlignedBB hitBox = entity.getEntityBoundingBox().expand(collisionSize, collisionSize, collisionSize);

			MovingObjectPosition result = hitBox.calculateIntercept(eyes, maxTracePos);

			if (result != null) {
				mc().playerController.interactWithEntitySendPacket(player(), entity);
				return;
			}
		}
	}

	/**
	 * @return whether the entity is a NPC.
	 */
	private boolean isNPC(Entity entity) {
		if (!(entity instanceof EntityOtherPlayerMP))
			return false;

		EntityOtherPlayerMP npc = ((EntityOtherPlayerMP) entity);
		GameProfile gp = npc.getGameProfile();
		return gp.getName().startsWith("§") && mc().getNetHandler().getPlayerInfo(gp.getId()) != null;
	}
}
