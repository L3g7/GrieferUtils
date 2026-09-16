/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.player.self_disguise.Disguise;
import net.minecraft.entity.monster.EntityCreeper;

/**
 * Disguised as {@link EntityCreeper}.
 */
public class CreeperDisguise extends Disguise<EntityCreeper> {
	public CreeperDisguise() {
		super(EntityCreeper.class);
	}

	@Override
	public EntityCreeper create(Arguments arguments) {
		EntityCreeper entity = super.create(arguments);
		if (arguments.getLiteral("powered"))
			entity.getDataWatcher().updateObject(17, (byte) 1);

		return entity;
	}
}
