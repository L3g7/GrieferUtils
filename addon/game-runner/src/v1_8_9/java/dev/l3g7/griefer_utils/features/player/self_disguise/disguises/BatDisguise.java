/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.player.self_disguise.Disguise;
import net.minecraft.entity.passive.EntityBat;

/**
 * Disguised as {@link EntityBat}.
 */
public class BatDisguise extends Disguise<EntityBat> {
	public BatDisguise() {
		super(EntityBat.class);
	}

	@Override
	public EntityBat create(Arguments arguments) {
		EntityBat entity = super.create(arguments);
		entity.setIsBatHanging(false);
		return entity;
	}
}
