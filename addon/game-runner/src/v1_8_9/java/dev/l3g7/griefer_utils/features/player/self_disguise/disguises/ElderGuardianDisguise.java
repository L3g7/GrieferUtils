/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.player.self_disguise.Disguise;
import net.minecraft.entity.monster.EntityGuardian;

/**
 * Disguised as {@link EntityGuardian} (elder).
 */
public class ElderGuardianDisguise extends Disguise<EntityGuardian> {
	public ElderGuardianDisguise() {
		super(EntityGuardian.class);
	}

	@Override
	public EntityGuardian create(Arguments arguments) {
		EntityGuardian entity = super.create(arguments);
		entity.setElder();
		return entity;
	}
}
