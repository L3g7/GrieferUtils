/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules.laby4;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.modules.Laby4Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class HeadOwner extends Laby4Module {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("HeadOwner")
		.description("Zeigt dir den Spieler, dessen Kopf du ansiehst.")
		.icon("steve");

	@Override
	public String getValue() {
		TileEntity e = rayTraceTileEntity();
		if (e instanceof TileEntitySkull s) {
			if (s.getPlayerProfile() == null || s.getPlayerProfile().getName() == null || s.getPlayerProfile().getName().isEmpty())
				return "Kein Spielerkopf";

			return s.getPlayerProfile().getName();
		}

		Entity entity = rayTraceEntity();

		if (entity == null)
			return "Kein Spielerkopf";

		// Get item
		ItemStack item;

		if (entity instanceof EntityArmorStand)
			item = ((EntityArmorStand) entity).getCurrentArmor(3);
		else
			item = ((EntityItemFrame) entity).getDisplayedItem();

		if (item == null || !(item.getItem() instanceof ItemSkull))
			return "Kein Spielerkopf";

		if (item.getMetadata() != 3 || !item.hasTagCompound())
			return "Kein Spielerkopf";

		// Extract name
		NBTTagCompound tag = item.getTagCompound();

		if (tag.hasKey("SkullOwner", 8))
			return tag.getString("SkullOwner");

		if (tag.hasKey("SkullOwner", 10)) {
			NBTTagCompound nbttagcompound = tag.getCompoundTag("SkullOwner");

			if (nbttagcompound.hasKey("Name", 8))
				return nbttagcompound.getString("Name");
		}

		return "Kein Spielerkopf";
	}

	@Override
	public boolean isVisibleInGame() {
		return rayTraceTileEntity() instanceof TileEntitySkull || rayTraceEntity() != null;
	}

    private TileEntity rayTraceTileEntity() {
        if (mc().thePlayer == null)
            return null;

        MovingObjectPosition rayTraceResult = mc().thePlayer.rayTrace(1000, 1);
        if (rayTraceResult == null // Should only happen when one of the player's coordinates is NaN, but it happens
	        || rayTraceResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
            return null;

        return mc().theWorld.getTileEntity(rayTraceResult.getBlockPos());
    }

	/**
	 * Source: EntityRenderer.getMouseOver(float partialTicks)
	 */
	private Entity rayTraceEntity() {

		Entity pointedEntity = null;

		double distance = 1000;

		Entity renderEntity = mc().getRenderViewEntity();

		if (renderEntity == null)
			return null;

		Vec3 look = renderEntity.getLook(1);
		Vec3 eyes = renderEntity.getPositionEyes(1);
		Vec3 maxEyes = eyes.addVector(look.xCoord * 1000, look.yCoord * 1000, look.zCoord * 1000);

		List<Entity> loadedEntities = mc().theWorld.loadedEntityList;

		for (Entity entity : loadedEntities) {
			if (!(entity instanceof EntityArmorStand) && !(entity instanceof EntityItemFrame))
				continue;

			float borderSize = entity.getCollisionBorderSize();
			AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);

			// Player is inside a searched entity
			if (axisalignedbb.isVecInside(eyes))
				return entity;

			MovingObjectPosition position = axisalignedbb.calculateIntercept(eyes, maxEyes);

			// Not hitting
			if (position == null)
				continue;

			double newDistance = eyes.distanceTo(position.hitVec);

			// Make sure only the closest one is chosen
			if (newDistance < distance) {
				pointedEntity = entity;
				distance = newDistance;
			}
		}

		return pointedEntity;
	}

}
