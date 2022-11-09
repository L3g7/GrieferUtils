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
import dev.l3g7.griefer_utils.settings.elements.RadioSetting;
import dev.l3g7.griefer_utils.util.misc.Vec3d;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.util.glu.Disk;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.features.render.Trajectories.TrajectoryMode.DISABLED;
import static dev.l3g7.griefer_utils.features.render.Trajectories.TrajectoryMode.TRAIL;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static dev.l3g7.griefer_utils.util.render.GlEngine.pos;
import static dev.l3g7.griefer_utils.util.render.GlEngine.*;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import static net.minecraft.block.material.Material.water;
import static net.minecraft.init.Items.*;
import static net.minecraft.util.EnumFacing.Axis.X;
import static net.minecraft.util.EnumFacing.Axis.Y;
import static net.minecraft.util.EnumFacing.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.GLU_SILHOUETTE;

/**
 * Shows the trajectory of the currently held projectile.
 */
@Singleton
public class Trajectories extends Feature {

	@MainElement
	private final RadioSetting<TrajectoryMode> mode = new RadioSetting<>(TrajectoryMode.class)
		.name("Trajectories")
		.description("Zeigt dir die Flugbahn des gehaltenen Items an.")
		.icon("marker")
		.defaultValue(DISABLED);

	private final Disk circle = new Disk();
	{ circle.setDrawStyle(GLU_SILHOUETTE); }

	/**
	 * Copyright (C) LiquidBounce 2020, GNU General Public License v3.0<br>
	 * See <a href="https://github.com/CCBlueX/LiquidBounce/blob/9c546f0598843e315f26f35c6e0c31d211f55276/shared/main/java/net/ccbluex/liquidbounce/features/module/modules/render/Projectiles.kt">Projectiles.kt</a>
	 * <p>
	 * converted to java, integrated into GrieferUtils (rendering & checks)
	 */
	@EventListener
	public void onRender(RenderWorldLastEvent event) {
		if (mode.get() == TrajectoryMode.DISABLED || player() == null)
			return;

		TrajectoryProperties p = getTrajectoryProperties();
		if (p == null)
			return;

		Vec3d renderPos = renderPos();

		// Calculate position
		float pitchDeg = player().rotationPitch;
		float pitch = (float) Math.toRadians(pitchDeg);
		float yaw = (float) Math.toRadians(player().rotationYaw);

		Vec3d pos = renderPos.subtract(Math.cos(yaw) * 0.16f, 0, Math.sin(yaw) * 0.16f)
		                          .add(0, player().getEyeHeight() - 0.1f, 0);

		// Calculate motion
		Vec3d motion = new Vec3d(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(Math.toRadians(pitchDeg + p.pitchOffset)), Math.cos(yaw) * Math.cos(pitch));

		if (!p.isBow)
			motion = motion.scale(0.4);
		motion = motion.normalize();
		motion = motion.scale(p.motionFactor);

		MovingObjectPosition landingPosition = null;
		boolean hasLanded = false;
		boolean hitEntity = false;

		begin();

		// Begin rendering
		disableDepth();
		enable(GL_BLEND, GL_LINE_SMOOTH);
		disable(GL_ALPHA_TEST, GL_TEXTURE_2D);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
		color(WHITE, .3f);
		glLineWidth(2);

		beginWorldDrawing(3, DefaultVertexFormats.POSITION);

		while (!hasLanded && pos.y > 0.0D) {
			Vec3d posBefore = new Vec3d(pos.x, pos.y, pos.z);
			Vec3d posAfter = new Vec3d(pos.x + motion.x, pos.y + motion.y, pos.z + motion.z);

			// Check block collision
			landingPosition = world().rayTraceBlocks(posBefore, posAfter, false, true, false);
			if (landingPosition != null) {
				hasLanded = true;
				posAfter = new Vec3d(landingPosition.hitVec.xCoord, landingPosition.hitVec.yCoord, landingPosition.hitVec.zCoord);
			}

			// Check entity collision
			AxisAlignedBB aabb = axisAlignedBB(pos.subtract(p.size), pos.add(p.size))
				.addCoord(motion.x, motion.y, motion.z)
				.expand(1, 1, 1);

			MovingObjectPosition collision = checkForEntityCollision(aabb, p.size, posBefore, posAfter);
			if (collision != null) {
				landingPosition = collision;
				hitEntity = hasLanded = true;
			}

			// Move
			pos = pos.add(motion);
			motion = motion
				.scale(blockAt(pos).getMaterial() == water ? 0.6 : p.motionSlowdown)
				.subtract(0, p.gravity, 0);

			// Draw trail
			if (mode.get() == TRAIL)
				pos(pos.subtract(renderPos)).endVertex();
		}

		endWorldDrawing();

		// Draw landing point
		translate(pos.subtract(renderPos));

		color(hitEntity ? RED : WHITE);

		if (landingPosition != null) {

			// Rotate based on side hit
			EnumFacing side = landingPosition.sideHit;
			if (side == UP || side == DOWN)
				rotate(90, X);
			else if (side == EAST || side == WEST)
				rotate(90, Y);
		}

		circle.draw(0, .2f, 40, 1);

		finish();
	}

	private MovingObjectPosition checkForEntityCollision(AxisAlignedBB arrowBox, float size, Vec3d posBefore, Vec3d posAfter) {
		int chunkMinX = (int) ((arrowBox.minX - 2) / 16);
		int chunkMaxX = (int) ((arrowBox.maxX + 2) / 16);
		int chunkMinZ = (int) ((arrowBox.minZ - 2) / 16);
		int chunkMaxZ = (int) ((arrowBox.maxZ + 2) / 16);

		List<Entity> collidedEntities = new ArrayList<>();

		// Get entities within AABB
		for (int x = chunkMinX; x <= chunkMaxX; x++)
			for (int z = chunkMinZ; z <= chunkMaxZ; z++)
				world().getChunkFromChunkCoords(x, z).getEntitiesWithinAABBForEntity(player(), arrowBox, collidedEntities, null);

		// Check if entities intercept
		for (Entity possibleEntity : collidedEntities) {
			if (possibleEntity.canBeCollidedWith() && possibleEntity != player()) {
				MovingObjectPosition intercept = possibleEntity.getEntityBoundingBox().expand(size, size, size).calculateIntercept(posBefore, posAfter);
				if (intercept != null)
					return intercept;
			}
		}
		return null;
	}

	private TrajectoryProperties getTrajectoryProperties() {
		TrajectoryProperties properties = new TrajectoryProperties();

		ItemStack heldItem = player().getHeldItem();
		if (heldItem == null)
			return null;

		Item item = heldItem.getItem();

		if (item == Items.bow) {
			if (!player().isUsingItem())
				return null;

			properties.isBow = true;
			properties.size = 0.3f;

			float power = player().getItemInUseDuration() / 20f;
			power = (power * power + power * 2f) / 3f;

			if (power < 0.1F)
				return null;

			properties.motionFactor = Math.min(power, 1f) * 3f;
		} else if (item == fishing_rod) {
			properties.gravity = 0.04f;
			properties.motionSlowdown = 0.92f;
		} else if (item == potionitem && ItemPotion.isSplash(heldItem.getItemDamage())) {
			properties.motionFactor = 0.5f;
			properties.pitchOffset = -20;
		} else if (item == snowball || item == ender_pearl || item == egg) {
			properties.gravity = 0.03f;
		} else
			return null;

		return properties;
	}

	private static class TrajectoryProperties {
		private boolean isBow = false;
		private float size = 0.25f, gravity = 0.05f, motionFactor = 1.5f, motionSlowdown = 0.99f, pitchOffset = 0;
	}

	@SuppressWarnings("unused")
	enum TrajectoryMode {

		DISABLED("Aus"), TRAIL("Flugbahn"), DOT("Punkt");

		final String name;

		TrajectoryMode(String name) {
			this.name = name;
		}
	}

}