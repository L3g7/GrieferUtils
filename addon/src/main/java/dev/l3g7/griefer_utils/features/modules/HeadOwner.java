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

package dev.l3g7.griefer_utils.features.modules;

import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import net.labymod.settings.elements.ControlElement.IconData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.List;

@Singleton
public class HeadOwner extends Module {

    public HeadOwner() {
        super("HeadOwner", "Zeigt dir den Spieler, dessen Kopf du ansiehst.", "head-owner", new IconData("griefer_utils/icons/steve.png"));
    }

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

		EntityArmorStand armorStand = rayTraceArmorStand();

		// Source: ItemSkull.getItemStackDisplayName(ItemStack stack)
	    if (armorStand == null)
			return getDefaultValues();

	    ItemStack item = armorStand.getCurrentArmor(3);
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
        return super.isShown() && (rayTraceTileEntity() instanceof TileEntitySkull || rayTraceArmorStand() != null);
    }

    private TileEntity rayTraceTileEntity() {
        if (mc.thePlayer == null)
            return null;

        MovingObjectPosition rayTraceResult = mc.thePlayer.rayTrace(1000, 1);
        if (rayTraceResult == null // Should only happen when one of the player's coordinates is NaN, but it happens (See https://discord.com/channels/1012404337433116792/1034951755240321146)
	        || rayTraceResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
            return null;

        return mc.theWorld.getTileEntity(rayTraceResult.getBlockPos());
    }

	/**
	 * Source: EntityRenderer.getMouseOver(float partialTicks)
	 */
	private EntityArmorStand rayTraceArmorStand() {

		Entity pointedEntity = null;

		double distance = 1000;

		Entity renderEntity = mc.getRenderViewEntity();

		if (renderEntity == null)
			return null;

		Vec3 look = renderEntity.getLook(1);
		Vec3 eyes = renderEntity.getPositionEyes(1);
		Vec3 maxEyes = eyes.addVector(look.xCoord * 1000, look.yCoord * 1000, look.zCoord * 1000);

		List<Entity> loadedEntities = mc.theWorld.loadedEntityList;

		for (Entity entity : loadedEntities) {
			if (!(entity instanceof EntityArmorStand))
				continue;

			float borderSize = entity.getCollisionBorderSize();
			AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);

			// Player is inside an armor stand
			if (axisalignedbb.isVecInside(eyes))
				return (EntityArmorStand) entity;

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

		return (EntityArmorStand) pointedEntity;
	}

}