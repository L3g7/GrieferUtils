/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.features.modules;

import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.Module;
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

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class HeadOwner extends Module {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("HeadOwner")
		.description("Zeigt dir den Spieler, dessen Kopf du ansiehst.")
		.icon("steve");

    @Override
    public String[] getDefaultValues() {
        return new String[]{"Kein Spielerkopf"};
    }

    @Override
    public String[] getValues() {
        TileEntity e = rayTraceTileEntity();
        if (e instanceof TileEntitySkull) {
            TileEntitySkull s = (TileEntitySkull) e;
            if (s.getPlayerProfile() != null && s.getPlayerProfile().getName() != null && !s.getPlayerProfile().getName().isEmpty())
                return new String[]{s.getPlayerProfile().getName()};
			return getDefaultValues();
        }

		Entity entity = rayTraceEntity();

		// Source: ItemSkull.getItemStackDisplayName(ItemStack stack)
	    if (entity == null)
			return getDefaultValues();

		ItemStack item;

		if (entity instanceof EntityArmorStand)
	        item = ((EntityArmorStand) entity).getCurrentArmor(3);
		else
			item = ((EntityItemFrame) entity).getDisplayedItem();

		if (item == null || !(item.getItem() instanceof ItemSkull))
			return getDefaultValues();

		if (item.getMetadata() != 3 || !item.hasTagCompound())
			return getDefaultValues();

	    NBTTagCompound tag = item.getTagCompound();

	    if (tag.hasKey("SkullOwner", 8))
		    return new String[]{tag.getString("SkullOwner")};

	    if (tag.hasKey("SkullOwner", 10)) {
		    NBTTagCompound nbttagcompound = tag.getCompoundTag("SkullOwner");

		    if (nbttagcompound.hasKey("Name", 8))
			    return new String[]{nbttagcompound.getString("Name")};
	    }

	    return getDefaultValues();
    }

    @Override
    public boolean isShown() {
        return super.isShown() && (rayTraceTileEntity() instanceof TileEntitySkull || rayTraceEntity() != null);
    }

    private TileEntity rayTraceTileEntity() {
        if (mc().thePlayer == null)
            return null;

        MovingObjectPosition rayTraceResult = mc().thePlayer.rayTrace(1000, 1);
        if (rayTraceResult == null // Should only happen when one of the player's coordinates is NaN, but it happens (See https://discord.com/channels/1012404337433116792/1034951755240321146)
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