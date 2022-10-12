package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.RadioSetting;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.util.glu.Cylinder;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.block.material.Material.water;
import static org.lwjgl.opengl.GL11.*;

@Singleton
public class Trajectories extends Feature {

	private final RadioSetting<TrajectoriesMode> mode = new RadioSetting<>(TrajectoriesMode.class)
		.name("Trajectories")
		.icon("marker")
		.defaultValue(TrajectoriesMode.DISABLED)
		.config("features.trajectories.mode")
		.stringProvider(TrajectoriesMode::getName);

	private enum TrajectoriesMode {

		DISABLED("Aus"), TRAIL("Trail"), DOT("Punkt");

		private final String name;

		TrajectoriesMode(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	public Trajectories() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return mode;
	}

	/**
	 *
	 * Copyright (C) LiquidBounce 2020, GNU General Public License v3.0<br>
	 * See <a href="https://github.com/CCBlueX/LiquidBounce/blob/9c546f0598843e315f26f35c6e0c31d211f55276/shared/main/java/net/ccbluex/liquidbounce/features/module/modules/render/Projectiles.kt">Projectiles.kt</a>
	 * <p>
	 * converted to Java, added mode checks, removed colorMode, cleaned the code up<br>
	 *
	 */
	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		if (mode.get() == TrajectoriesMode.DISABLED
			|| !isCategoryEnabled()
			|| !isOnGrieferGames()
			|| player() == null
			|| player().getHeldItem() == null)
			return;

		ItemStack heldItem = player().getHeldItem();
		if (heldItem == null)
			return;

		Item item = heldItem.getItem();
		RenderManager renderManager = mc().getRenderManager();
		boolean isBow = false;
		float motionFactor = 1.5F;
		float motionSlowdown = 0.99F;
		float size = 0.25f;
		float power = player().rotationYaw;
		float gravity = 0.05f;

		if (item instanceof ItemBow) {
			if (!player().isUsingItem())
				return;

			isBow = true;
			size = 0.3F;

			power = player().getItemInUseDuration() / 20f;
			power = (power * power + power * 2f) / 3f;

			if (power < 0.1F)
				return;

			motionFactor = Math.min(power, 1f) * 3f;
		} else if (item instanceof ItemFishingRod) {
			gravity = 0.04F;
			motionSlowdown = 0.92F;
		} else if (item instanceof ItemPotion && ItemPotion.isSplash(heldItem.getItemDamage())) {
			motionFactor = 0.5f;
		} else if (item instanceof ItemSnowball || item instanceof ItemEnderPearl || item instanceof ItemEgg) {
			gravity = 0.03f;
		} else
			return;

		final double renderPosX = Reflection.get(renderManager, "renderPosX", "field_78725_b", "o");
		final double renderPosY = Reflection.get(renderManager, "renderPosY", "field_78726_c", "p");
		final double renderPosZ = Reflection.get(renderManager, "renderPosZ", "field_78723_d", "q");

		float pitch = player().rotationPitch;
		byte pitchOffset = (byte) ((item instanceof ItemPotion && ItemPotion.isSplash(heldItem.getItemDamage())) ? -20 : 0);

		float pitchRadians = (float) Math.toRadians(pitch);
		float powerRadians = (float) Math.toRadians(power);
		float offsetPitchRadians = (float) Math.toRadians(pitch + pitchOffset);

		Vector3d pos = new Vector3d (
			renderPosX - (MathHelper.cos(powerRadians) * 0.16F),
			renderPosY + player().getEyeHeight() - 0.10000000149011612D,
			renderPosZ - (MathHelper.sin(powerRadians) * 0.16F)
		);

		Vector3d motion = new Vector3d (
			-MathHelper.sin(powerRadians) * MathHelper.cos(pitchRadians),
			-MathHelper.sin(offsetPitchRadians),
			MathHelper.cos(powerRadians) * MathHelper.cos(pitchRadians)
		);

		if (!isBow)
			motion.scale(0.4);
		motion.scale(1 / motion.length());
		motion.scale(motionFactor);

		MovingObjectPosition landingPosition = null;
		boolean hasLanded = false;
		boolean hitEntity = false;
		WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

		glDepthMask(false);
		glEnable(GL_BLEND);
		glEnable(GL_LINE_SMOOTH);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_ALPHA_TEST);
		glDisable(GL_TEXTURE_2D);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
		glColor4f(0, 160 / 255f, 1, 1);
		glLineWidth(2);
		worldRenderer.begin(3, DefaultVertexFormats.POSITION);

		while (!hasLanded && pos.y > 0.0D) {
			Vec3 posBefore = new Vec3(pos.x, pos.y, pos.z);
			Vec3 posAfter = new Vec3(pos.x + motion.x, pos.y + motion.y, pos.z + motion.z);

			landingPosition = world().rayTraceBlocks(posBefore, posAfter, false, true, false);
			posBefore = new Vec3(pos.x, pos.y, pos.z);
			posAfter = new Vec3(pos.x + motion.x, pos.y + motion.y, pos.z + motion.z);

			if (landingPosition != null) {
				hasLanded = true;
				posAfter = new Vec3(landingPosition.hitVec.xCoord, landingPosition.hitVec.yCoord, landingPosition.hitVec.zCoord);
			}

			AxisAlignedBB arrowBox = (new AxisAlignedBB(
				pos.x - size,
				pos.y - size,
				pos.z - size,
				pos.x + size,
				pos.y + size,
				pos.z + size))
				.addCoord(motion.x, motion.y, motion.z)
				.expand(1.0D, 1.0D, 1.0D);

			int chunkMinX = MathHelper.floor_double((arrowBox.minX - 2.0D) / 16.0D);
			int chunkMaxX = MathHelper.floor_double((arrowBox.maxX + 2.0D) / 16.0D);
			int chunkMinZ = MathHelper.floor_double((arrowBox.minZ - 2.0D) / 16.0D);
			int chunkMaxZ = MathHelper.floor_double((arrowBox.maxZ + 2.0D) / 16.0D);

			List<Entity> collidedEntities = new ArrayList<>();

			for (int x = chunkMinX; x <= chunkMaxX; x++)
				for (int z = chunkMinZ; z <= chunkMaxZ; z++)
					world().getChunkFromChunkCoords(x, z).getEntitiesWithinAABBForEntity(player(), arrowBox, collidedEntities, null);

			for (Entity possibleEntity : collidedEntities) {
				if (possibleEntity.canBeCollidedWith() && possibleEntity != player()) {
					AxisAlignedBB possibleEntityBoundingBox = possibleEntity.getEntityBoundingBox().expand(size, size, size);
					MovingObjectPosition movingObjectPosition = possibleEntityBoundingBox.calculateIntercept(posBefore, posAfter);
					if (movingObjectPosition != null) {
						hitEntity = true;
						hasLanded = true;
						landingPosition = movingObjectPosition;
					}
				}
			}

			pos.add(motion);

			IBlockState blockState = world().getBlockState(new BlockPos(pos.x, pos.y, pos.z));
			Block block = blockState.getBlock();

			motion.scale(block.getMaterial() == water ? 0.6 : motionSlowdown);

			motion.y -= gravity;

			if (mode.get() == TrajectoriesMode.TRAIL)
				worldRenderer.pos(
					pos.x - renderPosX,
					pos.y - renderPosY,
					pos.z - renderPosZ).endVertex();
		}

		Tessellator.getInstance().draw();

		glPushMatrix();
		glTranslated(
			pos.x - renderPosX,
			pos.y - renderPosY,
			pos.z - renderPosZ
		);

		if (landingPosition != null) {
			int ordinal = landingPosition.sideHit.ordinal();

			if (ordinal == 0)
				glRotatef(90f, 0f, 0f, 1f);
			if (ordinal == 2)
				glRotatef(90f, 1f, 0f, 0f);

			if (hitEntity)
				glColor4f(1, 0, 0, 150f / 255f);
		}

		glRotatef(-90f, 1f, 0f, 0f);
		Cylinder cylinder = new Cylinder();
		cylinder.setDrawStyle(100011);
		cylinder.draw(0.2F, 0f, 0f, 60, 1);

		glPopMatrix();
		glDepthMask(true);
		glDisable(GL_BLEND);
		glDisable(GL_LINE_SMOOTH);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_ALPHA_TEST);
		glEnable(GL_TEXTURE_2D);
		glColor4f(1f, 1f, 1f, 1f);
	}

}
