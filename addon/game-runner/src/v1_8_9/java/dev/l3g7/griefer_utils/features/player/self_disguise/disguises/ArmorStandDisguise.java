/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.player.self_disguise.Disguise;
import net.minecraft.entity.item.EntityArmorStand;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

/**
 * Disguised as {@link EntityArmorStand}.
 */
public class ArmorStandDisguise extends Disguise<EntityArmorStand> {

	public ArmorStandDisguise() {
		super(EntityArmorStand.class);
	}

	@Override
	public EntityArmorStand create(Arguments arguments) {
		boolean showArms = arguments.getLiteral("show_arms");

		EntityArmorStand entity = super.create(arguments);
		for (int i = 0; i < 4; i++)
			entity.setCurrentItemOrArmor(i + 1, player().inventory.armorInventory[i]);

		byte flags = entity.getDataWatcher().getWatchableObjectByte(10);
		if (showArms)
			flags = (byte)(flags | 4);
		else
			flags = (byte)(flags & -5);

		entity.getDataWatcher().updateObject(10, flags);
		return entity;
	}
}
